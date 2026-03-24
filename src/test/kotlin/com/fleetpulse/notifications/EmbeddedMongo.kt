package com.fleetpulse.notifications

import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker

object EmbeddedMongo {
    fun start(): TransitionWalker.ReachedState<RunningMongodProcess> {
        return Mongod.instance().start(Version.Main.V6_0)
    }

    fun getConnectionString(running: TransitionWalker.ReachedState<RunningMongodProcess>): String {
        val serverAddress = running.current().serverAddress
        return "mongodb://${serverAddress.host}:${serverAddress.port}"
    }
}
