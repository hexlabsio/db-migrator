package io.klouds.migrator

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.serverless.AppLoader

const val PORT = 8080

fun main() {
    Root(mapOf()).apiRoutes().asServer(SunHttp(PORT)).start()
    println("Server started on port $PORT")
}

object RootApi : AppLoader {
    override fun invoke(environment: Map<String, String>): HttpHandler = Root(environment).apiRoutes()
}

class Root(environment: Map<String, String>) {
    fun apiRoutes(): RoutingHttpHandler = routes(
            "/" bind Method.POST to { Response(Status.OK).body("Hello") }
    )
}