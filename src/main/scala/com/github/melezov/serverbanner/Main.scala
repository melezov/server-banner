package com.github.melezov.serverbanner

object Main:
  enum ColorMode:
    case Detect, On, Off

  case class Config(bannerText: String, greeting: Option[String], colorMode: ColorMode)

  sealed trait Action
  object Action:
    case class Run(config: Config) extends Action
    case class Help(colorMode: ColorMode) extends Action
    case object Version extends Action
    case object NoArgs extends Action

  def parseArgs(args: Array[String]): Either[String, Action] =
    args match
      case Array() =>
        Right(Action.NoArgs)
      case _ =>
        var greeting = Option.empty[String]
        var bannerText = Option.empty[String]
        var colorMode = ColorMode.Detect
        var help = false
        var i = 0
        while i < args.length do
          args(i) match
            case "--greeting" =>
              if i + 1 >= args.length then
                return Left("Missing value for --greeting")
              greeting = Some(args(i + 1))
              i += 2
            case "--color" =>
              if i + 1 >= args.length then
                return Left("Missing value for --color")
              args(i + 1) match
                case "detect" => colorMode = ColorMode.Detect
                case "on"   => colorMode = ColorMode.On
                case "off"  => colorMode = ColorMode.Off
                case other  => return Left(s"Invalid value for --color: $other (expected: detect, on, off)")
              i += 2
            case "--version" =>
              return Right(Action.Version)
            case "--help" =>
              help = true
              i += 1
            case arg if arg.startsWith("-") =>
              return Left(s"Unknown option: $arg")
            case arg =>
              if bannerText.isDefined then
                return Left(s"Unexpected argument: $arg")
              bannerText = Some(arg)
              i += 1
        val normalizedGreeting = greeting.map(_.trim).filter(_.nonEmpty)
        if help then
          Right(Action.Help(colorMode))
        else
          bannerText match
            case Some(text) if text.nonEmpty => Right(Action.Run(Config(text, normalizedGreeting, colorMode)))
            case _ => Left("Missing banner text argument")

  def resolveColor(mode: ColorMode, fd: Int = 1): Boolean = mode match
    case ColorMode.On  => true
    case ColorMode.Off => false
    case ColorMode.Detect =>
      val noColor = System.getenv("NO_COLOR")
      val term = System.getenv("TERM")
      if noColor != null then false
      else if term == "dumb" then false
      else Platform.isatty(fd)

  def help(color: Boolean): String =
    def yellow(s: String) = if color then s"${Color.Yellow.ansiCode}$s${Color.AnsiReset}" else s
    def green(s: String) = if color then s"${Color.Green.ansiCode}$s${Color.AnsiReset}" else s
    s"""${yellow("server-banner")} v${EmbeddedResources.version}
       |${yellow("https://github.com/melezov/server-banner")}
       |
       |${yellow("Usage:")} server-banner [OPTIONS] <banner-text>
       |
       |${yellow("Arguments:")}
       |  <banner-text>            Text to render as ASCII art banner
       |
       |${yellow("Options:")}
       |  ${green("--version")}                Print version and exit
       |  ${green("--help")}                   Show this help message
       |  ${green("--greeting <text>")}        Greeting text displayed above the banner
       |  ${green("--color <detect|on|off>")}  Color output mode (default: detect)
       |
       |${yellow("Examples:")}
       |  server-banner --color off My-Server
       |  server-banner --greeting 'Such  a  *lovely*  place' HT-Cal-4N""".stripMargin

  def main(args: Array[String]): Unit =
    parseArgs(args) match
      case Right(Action.Run(config)) =>
        val disallowed = config.bannerText.filterNot(Slant.AllowedChars).distinct
        if disallowed.nonEmpty then
          System.err.println(s"Error: Characters ${disallowed.mkString("'", "', '", "'")} are not supported in banner text")
          System.err.println(s"Valid characters: A-Z, a-z, 0-9, underscore and hyphen")
          sys.exit(1)
        val color = resolveColor(config.colorMode)
        print(Banner.render(config.bannerText, config.greeting, color))
      case Right(Action.Version) =>
        println(EmbeddedResources.version)
      case Right(Action.Help(colorMode)) =>
        val color = resolveColor(colorMode)
        print(Banner.render(Banner.DefaultBannerText, Some(Banner.DefaultGreeting), color))
        println()
        println(help(color))
      case Right(Action.NoArgs) =>
        val color = resolveColor(ColorMode.Detect)
        print(Banner.render(Banner.DefaultBannerText, Some(Banner.DefaultGreeting), color))
        println()
        println(help(color))
        sys.exit(1)
      case Left(error) =>
        val color = resolveColor(ColorMode.Detect, fd = 2)
        System.err.println(s"Error: $error")
        System.err.println()
        System.err.println(help(color))
        sys.exit(1)
