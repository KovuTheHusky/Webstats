<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

$res = $mysqli->query("SELECT player_name FROM ws_players");
while ($row = $res->fetch_assoc()) {
	$player[] = $row['player_name'];
	if (!file_exists($_SERVER['DOCUMENT_ROOT'] . '/uploads/' . $row['player_name'] . '.png') || filemtime($_SERVER['DOCUMENT_ROOT'] . '/uploads/' . $row['player_name'] . '.png') < time() - 300)
		playerHead($row['player_name']);
}

?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="icon" href="/favicon.png" type="image/png">
<link rel="stylesheet" href="/assets/styles/global.css" type="text/css" />
<title>Players | Webstats</title>
</head>
<body>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_header.php'; ?>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <article>
    <div class="face_roll">
      <ul>
<?php foreach ($player as $p) { ?>
        <li><a href="/player/<?php echo strtolower($p); ?>"><img src="/uploads/<?php echo strtolower($p); ?>.png" alt="<?php echo $p; ?>" class="" /></a></li>
<?php } ?>
      </ul>
    </div>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>
