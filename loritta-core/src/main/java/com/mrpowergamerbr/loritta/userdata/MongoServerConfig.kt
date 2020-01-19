package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandManager
import com.mrpowergamerbr.loritta.commands.CommandOptions
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.listeners.nashorn.NashornEventHandler
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.TextChannel
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.*

class MongoServerConfig @BsonCreator constructor(
		@BsonProperty("_id")
		@get:[BsonIgnore]
		val guildId: String // Guild ID
) {
	@Deprecated("Please use ServerConfigs' fields")
	var commandPrefix = "+" // Command Prefix (example: +help or .help or etc)
	var disabledCommands = ArrayList<String>() // Comandos desativados
	@Deprecated("Please use ServerConfigs' fields")
	var deleteMessageAfterCommand = false // Deletar mensagem do comando após executar ele?
	@Deprecated("Please use ServerConfigs' fields")
	var localeId = "default"

	var commandOptions = HashMap<String, CommandOptions>() // Command Options
	// Os command options são salvos assim:
	// AbstractCommand.getClass().getSimpleName() - CommandOptions

	@Deprecated("Please use ServerConfigs' fields")
	var warnOnMissingPermission = false // Avisar quando a Loritta não tem permissão para falar em um canal específico
	@Deprecated("Please use ServerConfigs' fields")
	var warnOnUnknownCommand = true
	@Deprecated("Please use ServerConfigs' fields")
	var blacklistedChannels = ArrayList<String>() // Canais em que os comandos são bloqueados
	@Deprecated("Please use ServerConfigs' fields")
	var warnIfBlacklisted = false
	@Deprecated("Please use ServerConfigs' fields")
	var blacklistWarning = "{@user} Você não pode usar comandos no {@channel}, bobinho(a)!"
	var nashornCommands = ArrayList<NashornCommand>() // Comandos customizados

	var nashornEventHandlers = ArrayList<NashornEventHandler>()

	var joinLeaveConfig = WelcomerConfig()
	var musicConfig = MusicConfig()
	var youTubeConfig = YouTubeConfig()
	var livestreamConfig = LivestreamConfig()
	var starboardConfig = StarboardConfig()
	var eventLogConfig = EventLogConfig()
	var autoroleConfig = AutoroleConfig()
	var inviteBlockerConfig = InviteBlockerConfig()
	var permissionsConfig = PermissionsConfig()
	var moderationConfig = ModerationConfig()
	var serverListConfig = ServerListConfig()
	var miscellaneousConfig = MiscellaneousConfig()
	var defaultTextChannelConfig = TextChannelConfig("default")
	var textChannelConfigs = mutableListOf<TextChannelConfig>()

	var lastCommandReceivedAt = 0L

	fun getUserData(id: Long): GuildProfile {
		val t = this
		return transaction(Databases.loritta) {
			getUserDataIfExists(id) ?: GuildProfile.new {
				this.guildId = t.guildId.toLong()
				this.userId = id
				this.money = BigDecimal(0)
				this.quickPunishment = false
				this.xp = 0
				this.isInGuild = true
			}
		}
	}

	fun getUserDataIfExists(id: Long): GuildProfile? {
		return transaction(Databases.loritta) {
			GuildProfile.find { (GuildProfiles.guildId eq guildId.toLong()) and (GuildProfiles.userId eq id) }.firstOrNull()
		}
	}

	fun getTextChannelConfig(textChannel: TextChannel): TextChannelConfig {
		return getTextChannelConfig(textChannel.id)
	}

	fun getTextChannelConfig(id: String): TextChannelConfig {
		return textChannelConfigs.firstOrNull { it.id == id } ?: defaultTextChannelConfig
	}

	fun hasTextChannelConfig(textChannel: TextChannel): Boolean {
		return hasTextChannelConfig(textChannel.id)
	}

	fun hasTextChannelConfig(id: String): Boolean {
		return textChannelConfigs.firstOrNull { it.id == id } != null
	}

	fun getCommandOptionsFor(cmd: AbstractCommand): CommandOptions {
		if (cmd is NashornCommand) { // Se é um comando feito em Nashorn...
			// Vamos retornar uma configuração padrão!
			return CommandManager.DEFAULT_COMMAND_OPTIONS
		}

		val simpleName = cmd.javaClass.simpleName
		return when {
			// Se a configuração do servidor tem opções de comandos...
			commandOptions.containsKey(simpleName) -> commandOptions[simpleName]!!
			// Se as opções padrões de comandos possui uma opção "específica" para o comando
			loritta.legacyCommandManager.defaultCmdOptions.containsKey(simpleName) -> loritta.legacyCommandManager.defaultCmdOptions[simpleName]!!.newInstance() as CommandOptions
			// Se não, retorne as opções padrões
			else -> CommandManager.DEFAULT_COMMAND_OPTIONS
		}
	}
}