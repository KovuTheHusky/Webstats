<?php
$execution_time = microtime(true);
require_once ($_SERVER['DOCUMENT_ROOT'] . '/assets/includes/configuration.php');
require_once ($_SERVER['DOCUMENT_ROOT'] . '/assets/includes/functions.php');
$mysqli = new mysqli($db['host'], $db['user'], $db['pass'], $db['table'], $db['port']);
if ($mysqli->connect_errno) {
	echo "#" . $mysqli->connect_errno . ": " . $mysqli->connect_error;
}

// Find out if the server is online
$online = $mysqli->query('SELECT COUNT(0) FROM ws_uptimes WHERE uptime_expired = 0 LIMIT 1')->fetch_row()[0];

?>