const express = require("express");
const { exec } = require("child_process");
const fs = require("fs");
const crypto = require("crypto");
const mysql = require("mysql");

const app = express();

// Hardcoded credentials
const DB_PASSWORD = "SuperSecretP@ss123";
const JWT_SECRET = "my-hardcoded-jwt-secret";

const connection = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: DB_PASSWORD,
  database: "app",
});

// SQL Injection
app.get("/user", (req, res) => {
  const username = req.query.username;
  const query = "SELECT * FROM users WHERE username = '" + username + "'";
  connection.query(query, (err, results) => {
    res.json(results);
  });
});

// Command Injection
app.get("/ping", (req, res) => {
  const host = req.query.host;
  exec("ping -c 1 " + host, (err, stdout) => {
    res.send(stdout);
  });
});

// Path Traversal
app.get("/file", (req, res) => {
  const filename = req.query.name;
  const data = fs.readFileSync("/var/app/uploads/" + filename, "utf8");
  res.send(data);
});

// Insecure eval usage on user input
app.post("/calc", (req, res) => {
  const expression = req.body.expression;
  const result = eval(expression);
  res.json({ result });
});

// Weak cryptographic hash (MD5)
function hashPassword(password) {
  return crypto.createHash("md5").update(password).digest("hex");
}

// Insecure randomness for a security token
function generateToken() {
  return Math.random().toString(36).substring(2);
}

// Reflected XSS - unescaped user input written back to response
app.get("/search", (req, res) => {
  const q = req.query.q;
  res.send("<html><body>Results for: " + q + "</body></html>");
});

app.listen(3000);
