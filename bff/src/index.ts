import express from 'express';
import cors from 'cors';
import { createProxyMiddleware } from 'http-proxy-middleware';
import dotenv from 'dotenv';

dotenv.config();

const app = express();
const PORT = process.env.PORT;

app.use(cors());

app.use('/api/auth', createProxyMiddleware({
    target: process.env.USER_SERVICE_URL,
    changeOrigin: true,
    pathRewrite: {
        '^/api/auth': ''
    },
}))

app.use('/api', createProxyMiddleware({
    target: process.env.TASK_SERVICE_URL,
    changeOrigin: true,
}));

app.get('/health', (req, res) => {
    res.json({ status: 'UP' });
});

app.listen(PORT, () => {
    console.log(`bff running on port ${PORT}`)
})
