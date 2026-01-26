import express from 'express';
import cors from 'cors';
import { createProxyMiddleware } from 'http-proxy-middleware';
import dotenv from 'dotenv';
import { GoogleAuth } from 'google-auth-library';

dotenv.config();

const app = express();
const PORT = process.env.PORT;
const USER_SERVICE_URL = process.env.USER_SERVICE_URL;
const TASK_SERVICE_URL = process.env.TASK_SERVICE_URL;

// BffのCloud Runから別のCloud Runを呼ぶためには、IAM認証を通過するために、トークンIDをヘッダーに付与する必要がある
// https://docs.cloud.google.com/run/docs/authenticating/service-to-service?utm_source=chatgpt.com&hl=ja
const auth = new GoogleAuth();
let userIdTokenClientPromise: ReturnType<GoogleAuth['getIdTokenClient']> | null = null;
let taskIdTokenClientPromise: ReturnType<GoogleAuth['getIdTokenClient']> | null = null;

app.use(cors());

const attachUserServiceAuth = async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    if (!USER_SERVICE_URL) {
        next(new Error('USER_SERVICE_URL is not set'));
        return;
    }

    try {
        if (!userIdTokenClientPromise) {
            userIdTokenClientPromise = auth.getIdTokenClient(USER_SERVICE_URL);
        }

        const client = await userIdTokenClientPromise;
        const headers = await client.getRequestHeaders();
        const authHeader = headers['Authorization'] ?? headers['authorization'];
        if (authHeader) {
            req.headers['authorization'] = authHeader;
        }
        next();
    } catch (err) {
        next(err);
    }
};

app.use('/api/auth', attachUserServiceAuth, createProxyMiddleware({
    target: USER_SERVICE_URL,
    changeOrigin: true,
    pathRewrite: {
        '^/api/auth': ''
    },
}))

const attachTaskServiceAuth = async (req: express.Request, res: express.Response, next: express.NextFunction) => {
    if (!TASK_SERVICE_URL) {
        next(new Error('TASK_SERVICE_URL is not set'));
        return;
    }

    try {
        if (!taskIdTokenClientPromise) {
            taskIdTokenClientPromise = auth.getIdTokenClient(TASK_SERVICE_URL);
        }

        const client = await taskIdTokenClientPromise;
        const headers = await client.getRequestHeaders();
        const authHeader = headers['Authorization'] ?? headers['authorization'];
        if (authHeader) {
            req.headers['authorization'] = authHeader;
        }
        next();
    } catch (err) {
        next(err);
    }
};

app.use('/api', attachTaskServiceAuth, createProxyMiddleware({
    target: TASK_SERVICE_URL,
    changeOrigin: true,
}));

app.get('/health', (req, res) => {
    res.json({ status: 'UP' });
});

app.listen(PORT, () => {
    console.log(`bff running on port ${PORT}`)
})
