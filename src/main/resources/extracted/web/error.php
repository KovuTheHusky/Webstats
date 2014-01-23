<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

$error = $_GET['error'];
switch ($error) {
	case '100':
		$str = 'That block does not exist.';
	case '101':
		$str = 'That player does not exist.';
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="icon" href="/favicon.png" type="image/png">
<link rel="stylesheet" href="/assets/styles/global.css" type="text/css" />
<title>Error | Webstats</title>
</head>
<body>
  <header class="<?php echo $online ? 'green' : 'red'; ?>"></header>
  <nav>
    <ul class="box">
      <li>Text</li>
    </ul>
  </nav>
  <article>
    <span style="font-size: 32px;">
      <?php echo $error; ?>: <?php echo $str; ?>
    </span>
  </article>
  <footer>
    Data from <a href="http://dev.bukkit.org/bukkit-mods/webstats">Webstats</a> ${project.version} by <a href="http://codeski.com">Codeski</a>
  </footer>
</body>
</html>