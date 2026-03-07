// SAP HANA XS JavaScript file with multiple vulnerabilities

// Vulnerability 1: SQL Injection
function getUserData(userId) {
    var conn = $.db.getConnection();
    var query = "SELECT * FROM USERS WHERE USER_ID = '" + userId + "'"; // SQL Injection
    var statement = conn.prepareStatement(query);
    var result = statement.executeQuery();
    conn.close();
    return result;
}

// Vulnerability 2: Hardcoded credentials
var dbConfig = {
    host: "localhost",
    username: "admin",
    password: "P@ssw0rd123!", // Hardcoded password
    database: "SYSTEMDB"
};

// Vulnerability 3: Command Injection
function executeSystemCommand(userInput) {
    var command = "ls -la " + userInput; // Command injection
    var result = $.os.exec(command);
    return result;
}

// Vulnerability 4: Path Traversal
function readUserFile(filename) {
    var filePath = "/user/data/" + filename; // Path traversal vulnerability
    var file = $.import("", filePath);
    return file;
}

// Vulnerability 5: XSS vulnerability in response
function displayUserComment(comment) {
    $.response.contentType = "text/html";
    $.response.setBody("<html><body>" + comment + "</body></html>"); // XSS
}

// Vulnerability 6: Use of eval() with user input
function processUserScript(script) {
    var result = eval(script); // Dangerous use of eval
    return result;
}

// Vulnerability 7: Insecure random number generation
function generateToken() {
    var token = Math.random().toString(36).substring(2); // Weak random
    return token;
}

// Vulnerability 8: Information disclosure
function handleError(error) {
    $.response.status = $.net.http.INTERNAL_SERVER_ERROR;
    $.response.setBody(JSON.stringify({
        error: error.message,
        stack: error.stack, // Stack trace exposure
        config: dbConfig // Sensitive data exposure
    }));
}

// Vulnerability 9: LDAP Injection
function ldapSearch(userName) {
    var ldapFilter = "(uid=" + userName + ")"; // LDAP injection
    var result = $.security.ldap.search(ldapFilter);
    return result;
}

// Vulnerability 10: XXE vulnerability
function parseXMLData(xmlString) {
    var parser = new $.util.SAXParser();
    parser.parse(xmlString); // Potential XXE
    return parser.getResult();
}

// Vulnerability 11: Insecure deserialization
function deserializeUserData(data) {
    var userData = JSON.parse(data); // Unsafe deserialization
    return userData;
}

// Vulnerability 12: Missing authentication check
function deleteUser(userId) {
    // No authentication or authorization check
    var conn = $.db.getConnection();
    var query = "DELETE FROM USERS WHERE USER_ID = ?";
    var statement = conn.prepareStatement(query);
    statement.setInteger(1, userId);
    statement.executeUpdate();
    conn.close();
}

// Vulnerability 13: Sensitive data in logs
function logUserActivity(user) {
    $.trace.error("User activity: " + JSON.stringify({
        userId: user.id,
        password: user.password, // Password in logs
        creditCard: user.creditCard,
        ssn: user.ssn
    }));
}

// Main request handler with vulnerabilities
try {
    var userId = $.request.parameters.get("userId");
    var action = $.request.parameters.get("action");
    
    // No input validation
    if (action === "get") {
        var userData = getUserData(userId);
        $.response.setBody(JSON.stringify(userData));
    } else if (action === "delete") {
        deleteUser(userId);
        $.response.setBody("User deleted");
    } else if (action === "exec") {
        var cmd = $.request.parameters.get("cmd");
        var result = executeSystemCommand(cmd);
        $.response.setBody(result);
    }
    
} catch (e) {
    handleError(e);
}
