<?php
$execution_time = microtime(true);
require_once ($_SERVER['DOCUMENT_ROOT'] . '/assets/includes/configuration.php');
require_once ($_SERVER['DOCUMENT_ROOT'] . '/assets/includes/functions.php');
$mysqli = new mysqli($db['host'], $db['user'], $db['pass'], $db['table'], $db['port']);
if ($mysqli->connect_errno) {
	echo "#" . $mysqli->connect_errno . ": " . $mysqli->connect_error;
}

// Check if the uploads directory is writable
$uploads_writable = is_writable($_SERVER['DOCUMENT_ROOT'] . '/uploads');
if (!$uploads_writable)
	$error[] = 'Your uploads directory is not writable. You may want to set its permissions to 777. Its current permissions are ' . decoct(fileperms($_SERVER['DOCUMENT_ROOT'] . '/uploads') & 0777) . '.';

// Find out if the server is online
$online = $mysqli->query('SELECT COUNT(0) FROM ws_uptimes WHERE uptime_expired = 0 LIMIT 1')->fetch_row()[0];

?>