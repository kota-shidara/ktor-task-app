package com.example.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Cloud Run 上で動作している場合に、自身のサービス URL を Cloud Run Admin API から取得する。
 * K_SERVICE 環境変数（Cloud Run が自動注入）の有無で Cloud Run 環境かを判定する。
 */
object CloudRunSelfUrlResolver {

    private val logger = LoggerFactory.getLogger(CloudRunSelfUrlResolver::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Cloud Run 環境であれば自身のサービス URL を返す。
     * Cloud Run 環境でなければ null を返す。
     */
    fun resolve(projectId: String, region: String): String? {
        val serviceName = System.getenv("K_SERVICE") ?: return null

        logger.info("Cloud Run 環境を検出。サービス URL を解決します: $serviceName")

        val client = HttpClient.newHttpClient()
        val accessToken = fetchAccessToken(client)
        val serviceUri = fetchServiceUri(client, accessToken, projectId, region, serviceName)

        logger.info("サービス URL を解決しました: $serviceUri")
        return serviceUri
    }

    private fun fetchAccessToken(client: HttpClient): String {
        val request = HttpRequest.newBuilder()
            .uri(URI("http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token"))
            .header("Metadata-Flavor", "Google")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            error("メタデータサーバからアクセストークンの取得に失敗しました: status=${response.statusCode()}")
        }

        return json.parseToJsonElement(response.body())
            .jsonObject["access_token"]?.jsonPrimitive?.content
            ?: error("メタデータサーバのレスポンスに access_token が含まれていません")
    }

    private fun fetchServiceUri(
        client: HttpClient,
        accessToken: String,
        projectId: String,
        region: String,
        serviceName: String,
    ): String {
        val apiUrl = "https://run.googleapis.com/v2/projects/$projectId/locations/$region/services/$serviceName"
        val request = HttpRequest.newBuilder()
            .uri(URI(apiUrl))
            .header("Authorization", "Bearer $accessToken")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            error("Cloud Run Admin API からサービス情報の取得に失敗しました: status=${response.statusCode()}, body=${response.body()}")
        }

        return json.parseToJsonElement(response.body())
            .jsonObject["uri"]?.jsonPrimitive?.content
            ?: error("Cloud Run Admin API のレスポンスに uri が含まれていません")
    }
}
