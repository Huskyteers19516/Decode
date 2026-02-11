package dev.frozenmilk.dairy.mercurial.ftc

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import dev.frozenmilk.dairy.BuildConfig
import dev.frozenmilk.dairy.mercurial.continuations.Continuations
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Scheduler
import dev.frozenmilk.sinister.isPublic
import dev.frozenmilk.sinister.isStatic
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner
import dev.frozenmilk.sinister.targeting.WideSearch
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.psilynx.psikit.core.Logger
import org.psilynx.psikit.ftc.FtcLoggingSession
import org.psilynx.psikit.ftc.autolog.PsiKitNoAutoLog
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.kotlinProperty

@Suppress("UNUSED")
object MercurialProgramScanner : OpModeScanner() {
    override val targets = WideSearch()


    @PsiKitNoAutoLog
    class MercurialProgramConverter(
        private val metadata: OpModeMeta,
        private val program: Mercurial.Program,
    ) : LinearOpMode() {
        val rlogPort: Int = 5800

        /** Output folder for RLOGWriter. */
        val rlogFolder: String = "/sdcard/FIRST/PsiKit/"

        /** Optional filename override; blank means "use default". */
        val rlogFilename: String = ""

        val psiKitSession: FtcLoggingSession = FtcLoggingSession()

        private var sessionStarted: Boolean = false
        private var startHookRan: Boolean = false
        private var stopHookRan: Boolean = false
        private var lastObservedStarted: Boolean = false
        private var lastBeforeUserStart = Logger.getRealTimestamp()
        private var lastBeforeUserEnd = Logger.getRealTimestamp()

        override fun runOpMode() {
            if (BuildConfig.BUILD_TYPE == "debug") {
                ensurePsiKitStarted()
                internalStartOnce()
            }
            val scheduler = Scheduler.Standard()
            val context = Context(
                metadata,
                {
                    if (isStopRequested) State.STOP
                    else if (isStarted) State.LOOP
                    else State.INIT
                },
                scheduler,
                hardwareMap,
                telemetry,
                gamepad1,
                gamepad2,
                blackboard,
                { Pair(lastBeforeUserStart, lastBeforeUserEnd) },
            )

            context.run {
                val _ = schedule(
                    Continuations.loop(
                        exec {
                            lastBeforeUserStart = Logger.getRealTimestamp()

                            Logger.periodicBeforeUser()
                            psiKitSession.logOncePerLoop(this@MercurialProgramConverter)
                            maybeRunStartHookFromReplay()

                            lastBeforeUserEnd = Logger.getRealTimestamp()
                        }
                    ),
                )
                program.run {
                    exec()
                }
            }
            if (stopHookRan) return
            stopHookRan = true
            psiKitSession.end()
            sessionStarted = false
        }

        fun ensurePsiKitStarted() {
            if (sessionStarted) return

            if (rlogFilename.isNotBlank()) {
                psiKitSession.start(
                    this,
                    rlogPort,
                    filename = rlogFilename,
                    folder = rlogFolder,
                )
            } else {
                psiKitSession.start(
                    this,
                    rlogPort,
                    folder = rlogFolder,
                )
            }

            sessionStarted = true
        }

        fun maybeRunStartHookFromReplay() {
            if (startHookRan) return

            val startedNow = readBooleanFieldIfPresent(this, "isStarted") ?: false
            if (!lastObservedStarted && startedNow) {
                internalStartOnce()
            }
            lastObservedStarted = startedNow
        }

        private fun internalStartOnce() {
            if (startHookRan) return
            startHookRan = true
            // Keep the replay edge detector from firing later.
            lastObservedStarted = true
//            onPsiKitStart()
        }

        private fun readBooleanFieldIfPresent(target: Any, fieldName: String): Boolean? {
            var clazz: Class<*>? = target.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField(fieldName)
                    field.isAccessible = true
                    return field.getBoolean(target)
                } catch (_: NoSuchFieldException) {
                    clazz = clazz.superclass
                } catch (_: Throwable) {
                    return null
                }
            }
            return null
        }
    }

    private fun isProgram(cls: Class<*>) =
        Mercurial.RegisterableProgram::class.java.isAssignableFrom(cls)

    override fun scan(
        loader: ClassLoader,
        cls: Class<*>,
        registrationHelper: RegistrationHelper,
    ) {
        cls.declaredFields.filter { field ->
            field.type == Mercurial.ProgramRegistrar::class.java //
                    && field.isStatic()
        }.forEach { field ->
            field.isAccessible = true
            val registrar = field.get(null) as Mercurial.ProgramRegistrar

            registrar.register { registerableProgram ->
                val metadata = OpModeMeta.Builder() //
                    .setName(requireNotNull(registerableProgram.name) { "dynamic registration of a program must provide a name" }) //
                    .setGroup(registerableProgram.group ?: OpModeMeta.DefaultGroup) //
                    .setFlavor(registerableProgram.type) //
                    .setTransitionTarget(registerableProgram.transitionTarget?.invoke(registerableProgram.name)) //
                    .setSource(OpModeMeta.Source.ANDROID_STUDIO) //
                    .build() //

                registrationHelper.register(metadata) {
                    MercurialProgramConverter(
                        metadata,
                        registerableProgram.program,
                    )
                }
            }
        }

        cls.declaredFields.filter { field ->
            field.type == Mercurial.RegisterableProgram::class.java //
                    && field.kotlinProperty?.let { it.visibility == KVisibility.PUBLIC } ?: field.isPublic() //
                    && field.isStatic() //
        }.forEach {
            it.isAccessible = true
            val registerableProgram = it.get(null) as Mercurial.RegisterableProgram

            val metadata = OpModeMeta.Builder() //
                .setName(registerableProgram.name ?: it.name) //
                .setGroup(registerableProgram.group ?: OpModeMeta.DefaultGroup) //
                .setFlavor(registerableProgram.type) //
                .setTransitionTarget(
                    registerableProgram.transitionTarget?.invoke(
                        registerableProgram.name ?: it.name
                    )
                ) //
                .setSource(OpModeMeta.Source.ANDROID_STUDIO) //
                .build() //

            registrationHelper.register(metadata) {
                MercurialProgramConverter(
                    metadata,
                    registerableProgram.program,
                )
            }
        }
    }
}