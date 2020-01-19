package com.mrpowergamerbr.loritta.website.requests.routes.page.guild.configure

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import net.dv8tion.jda.api.entities.Guild
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import kotlin.collections.set

@Path("/:localeId/guild/:guildId/configure/badge")
class ConfigureCustomBadgeController {
	@GET
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_AUTH)
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>, @Local guild: Guild, @Local newServerConfig: ServerConfig, @Local serverConfig: MongoServerConfig, @Local userIdentification: SimpleUserIdentification): String {
		variables["saveType"] = "badge"
		return evaluate("configure_badge.html", variables)
	}
}