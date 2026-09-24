// Node.js config - fake test credentials for Checkmarx Secret Detection scanner testing

// rule: discord-api-token
const DISCORD_BOT_TOKEN = "FakeDiscordToken.Abcdef123456.GhIjKlMnOpQrStUvWxYz12";

// rule: gitlab-pat
const GITLAB_TOKEN = "glpat-Fake1234567890AbcdefGHIJ";

// rule: grafana-api-key
const GRAFANA_API_KEY = "eyJrIjoiRmFrZUdyYWZhbmFLZXlGb3JUZXN0aW5nMTIzNDU2Nzg5MCIsIm4iOiJ0ZXN0Iiwi";

// rule: heroku-api-key
const HEROKU_API_KEY = "12345678-abcd-1234-abcd-1234567890ab";

// rule: linear-api-key
const LINEAR_API_KEY = "lin_api_FakeLinearKeyForTesting1234567890ABCD";

// rule: notion-api-token
const NOTION_TOKEN = "secret_FakeNotionTokenForTesting1234567890ABCDEF";

module.exports = {
  DISCORD_BOT_TOKEN,
  GITLAB_TOKEN,
  GRAFANA_API_KEY,
  HEROKU_API_KEY,
  LINEAR_API_KEY,
  NOTION_TOKEN,
};
