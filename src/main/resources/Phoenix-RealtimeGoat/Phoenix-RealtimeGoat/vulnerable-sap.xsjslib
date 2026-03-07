// SAP HANA XS JavaScript Library with security vulnerabilities

// Vulnerability 1: SQL Injection in library function
function executeCustomQuery(tableName, columnName, value) {
    var conn = $.db.getConnection();
    // Direct string concatenation - SQL Injection
    var sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " = '" + value + "'";
    var statement = conn.prepareStatement(sql);
    var resultSet = statement.executeQuery();
    conn.close();
    return resultSet;
}

// Vulnerability 2: Hardcoded encryption key
var ENCRYPTION_KEY = "Fake_Key"; // Hardcoded secret
var API_KEY = "sk_test_fake_key_for_security_testing"; // Hardcoded API key
var JWT_SECRET = "super_secret_jwt_token_key"; // Hardcoded JWT secret

// Vulnerability 3: Weak cryptography
function encryptData(data) {
    var crypto = $.security.crypto;
    // Using weak MD5 algorithm
    var hash = crypto.md5(data);
    return hash;
}

// Vulnerability 4: Password handling vulnerability
function authenticateUser(username, password) {
    var conn = $.db.getConnection();
    // Storing password in plain text comparison
    var query = "SELECT * FROM USERS WHERE USERNAME = ? AND PASSWORD = ?";
    var stmt = conn.prepareStatement(query);
    stmt.setString(1, username);
    stmt.setString(2, password); // Plain text password
    var result = stmt.executeQuery();
    conn.close();
    return result.next();
}

// Vulnerability 5: XML External Entity (XXE) vulnerability
function processXMLFeed(xmlData) {
    var parser = $.util.SAXParser(true); // External entities enabled
    parser.parse(xmlData);
    return parser.root();
}

// Vulnerability 6: Path Traversal in file operations
function loadConfigFile(configName) {
    // No path validation - path traversal
    var filePath = "/config/" + configName + ".json";
    var file = $.net.http.readDestination("sap.hana.config", filePath);
    return JSON.parse(file.response.body.asString());
}

// Vulnerability 7: SSRF vulnerability
function fetchExternalData(url) {
    // No URL validation - SSRF
    var destination = $.net.http.readDestination("external", url);
    var client = new $.net.http.Client();
    var request = new $.net.http.Request($.net.http.GET, url);
    client.request(request, destination);
    var response = client.getResponse();
    return response.body.asString();
}

// Vulnerability 8: Command Injection in utility function
function compressFile(filename) {
    // Command injection through filename
    var command = "gzip " + filename;
    var result = $.os.exec(command);
    return result;
}

// Vulnerability 9: Insecure session management
function createUserSession(userId) {
    var sessionId = userId + "_" + Date.now(); // Predictable session ID
    $.session.setAttribute("sessionId", sessionId);
    $.session.setAttribute("userId", userId);
    return sessionId;
}

// Vulnerability 10: No CSRF protection
function updateUserProfile(userId, profileData) {
    // No CSRF token validation
    var conn = $.db.getConnection();
    var query = "UPDATE USERS SET EMAIL = ?, PHONE = ? WHERE USER_ID = ?";
    var stmt = conn.prepareStatement(query);
    stmt.setString(1, profileData.email);
    stmt.setString(2, profileData.phone);
    stmt.setInteger(3, userId);
    stmt.executeUpdate();
    conn.close();
}

// Vulnerability 11: Information disclosure
function getDatabaseConfig() {
    return {
        host: "db.company.com",
        port: 30015,
        username: "SYSTEM",
        password: "Manager123", // Exposed credentials
        schema: "PRODUCTION",
        ssl: false // Insecure connection
    };
}

// Vulnerability 12: Unsafe reflection
function invokeMethod(objectName, methodName, params) {
    // Dynamic method invocation without validation
    var obj = $[objectName];
    var method = obj[methodName];
    return method.apply(obj, params);
}

// Vulnerability 13: LDAP Injection
function findUserByEmail(email) {
    var ldap = $.security.ldap;
    // LDAP injection vulnerability
    var filter = "(mail=" + email + ")";
    var result = ldap.search({
        base: "ou=users,dc=company,dc=com",
        filter: filter,
        scope: ldap.SCOPE_SUBTREE
    });
    return result;
}

// Vulnerability 14: Insecure cookie handling
function setAuthCookie(token) {
    $.response.cookies.set({
        name: "auth_token",
        value: token,
        secure: false, // Cookie without secure flag
        httpOnly: false, // Cookie accessible via JavaScript
        sameSite: "None" // No CSRF protection
    });
}

// Vulnerability 15: Mass assignment vulnerability
function updateUserFromRequest() {
    var userData = {};
    var params = $.request.parameters;
    // No filtering of sensitive fields
    for (var key in params) {
        userData[key] = params[key]; // Mass assignment
    }
    return updateUser(userData);
}

// Vulnerability 16: Race condition
var globalCounter = 0;
function incrementCounter() {
    // Race condition - not thread-safe
    var temp = globalCounter;
    temp++;
    globalCounter = temp;
    return globalCounter;
}

// Export vulnerable functions
exports.executeCustomQuery = executeCustomQuery;
exports.encryptData = encryptData;
exports.authenticateUser = authenticateUser;
exports.processXMLFeed = processXMLFeed;
exports.loadConfigFile = loadConfigFile;
exports.fetchExternalData = fetchExternalData;
exports.compressFile = compressFile;
exports.createUserSession = createUserSession;
exports.updateUserProfile = updateUserProfile;
exports.getDatabaseConfig = getDatabaseConfig;
exports.invokeMethod = invokeMethod;
exports.findUserByEmail = findUserByEmail;
exports.setAuthCookie = setAuthCookie;
exports.ENCRYPTION_KEY = ENCRYPTION_KEY;
exports.API_KEY = API_KEY;
