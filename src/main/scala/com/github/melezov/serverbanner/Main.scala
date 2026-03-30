package com.github.melezov.serverbanner

object Main:
  case class Config(bannerText: String, greeting: Option[String], color: Boolean)

  def parseArgs(args: Array[String]): Either[String, Option[Config]] =
    args match
      case Array() =>
        Right(None)
      case _ =>
        var greeting = Option.empty[String]
        var bannerText = Option.empty[String]
        var color = true
        var i = 0
        while i < args.length do
          args(i) match
            case "--greeting" | "-g" =>
              if i + 1 >= args.length then
                return Left("Missing value for --greeting")
              greeting = Some(args(i + 1))
              i += 2
            case "--no-color" =>
              color = false
              i += 1
            case "--help" =>
              return Right(None)
            case arg if arg.startsWith("-") =>
              return Left(s"Unknown option: $arg")
            case arg =>
              if bannerText.isDefined then
                return Left(s"Unexpected argument: $arg")
              bannerText = Some(arg)
              i += 1
        bannerText match
          case Some(text) => Right(Some(Config(text, greeting, color)))
          case None => Left("Missing banner text argument")

  val help: String =
    """Usage: server-banner [OPTIONS] <banner-text>
      |
      |Arguments:
      |  <banner-text>            Text to render as ASCII art banner
      |
      |Options:
      |  -g, --greeting <text>    Greeting text displayed above the banner
      |  --no-color               Disable ANSI color output
      |  --help                   Show this help message
      |
      |Examples:
      |  server-banner My-Server
      |  server-banner --greeting 'Such  a  *lovely*  place' HT-California-02
      |  server-banner --no-color My-Server""".stripMargin

  def main(args: Array[String]): Unit =
    parseArgs(args) match
      case Right(Some(config)) =>
        print(Banner.render(config.bannerText, config.greeting, config.color))
      case Right(None) =>
        print(Banner.render(Banner.DefaultBannerText, Some(Banner.DefaultGreeting), true))
        println()
        println(help)
      case Left(error) =>
        System.err.println(s"Error: $error")
        System.err.println()
        System.err.println(help)
        sys.exit(1)
