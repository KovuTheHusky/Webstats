<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

// Player list and heads
$res = $mysqli->query("SELECT player_name FROM ws_players");
while ($row = $res->fetch_assoc()) {
	$player[] = $row['player_name'];
	if (!file_exists($_SERVER['DOCUMENT_ROOT'] . '/uploads/' . $row['player_name'] . '.png') || filemtime($_SERVER['DOCUMENT_ROOT'] . '/uploads/' . $row['player_name'] . '.png') < time() - 300)
		playerHead($row['player_name']);
}

// Uptime calculation
$time_since_first_start = $mysqli->query("SELECT TIMESTAMPDIFF(SECOND,uptime_start,'" . date('Y-m-d H:i:s') . "') FROM ws_uptimes ORDER BY uptime_start ASC LIMIT 1")->fetch_all()[0][0];
$uptime = array_sum(array_map('array_shift', $mysqli->query('SELECT TIMESTAMPDIFF(SECOND,uptime_start,uptime_end) FROM ws_uptimes WHERE uptime_expired = 1')->fetch_all()));
if ($online)
	$uptime += $mysqli->query("SELECT TIMESTAMPDIFF(SECOND,uptime_start,'" . date('Y-m-d H:i:s') . "') FROM ws_uptimes WHERE uptime_expired = 0 LIMIT 1")->fetch_all()[0][0];
$uptime = number_format($uptime / $time_since_first_start * 100, 2);
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="icon" href="/favicon.png" type="image/png">
<link rel="stylesheet" href="/assets/styles/global.css" type="text/css" />
<title>Webstats</title>
</head>
<body>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_header.php'; ?>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <article>
    <div class="face_roll">
      <ul>
<?php foreach ($player as $p) { ?>
        <li><a href="/player/<?php echo strtolower($p); ?>"><img src="/uploads/<?php echo strtolower($p); ?>.png" alt="<?php echo $p; ?>" <?php if (!in_array($p, $online_players)) { echo 'class="offline"'; } ?> /></a></li>
<?php } ?>
      </ul>
    </div>
    <section class="home">
      <div>
        <h1>Server</h1>
        <ul>
<?php if ($online) { ?>
          <li><?php echo $mysqli->query('SELECT uptime_start FROM ws_uptimes WHERE uptime_expired = 0 LIMIT 1')->fetch_row()[0]; ?> Startup</li>
<?php } else { ?>
          <li><?php echo $mysqli->query('SELECT uptime_end FROM ws_uptimes ORDER BY uptime_end DESC LIMIT 1')->fetch_row()[0]; ?> Shutdown</li>
<?php } ?>
          <li><?php echo $uptime; ?>% Uptime</li>
        </ul>
      </div>
      <div>
        <h1>Players</h1>
        <ul>
          <li><?php echo $mysqli->query('SELECT COUNT(0) FROM ws_sessions WHERE session_expired = 0')->fetch_row()[0]; ?> Online</li>
          <li><?php echo $mysqli->query('SELECT COUNT(0) FROM ws_players')->fetch_row()[0]; ?> Tracked</li>
          <li>? Peak</li>
          <li>? Maximum</li>
        </ul>
      </div>
      <div>
        <h1>Damages</h1>
        <ul>
          <li>No data shown here yet.</li>
        </ul>
      </div>
      <div>
        <h1>Distances</h1>
        <ul>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances")->fetch_row()[0]); ?> Total</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'WALKED'")->fetch_row()[0]); ?> Walked</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'SWAM'")->fetch_row()[0]); ?> Swam</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'FALLED'")->fetch_row()[0]); ?> Fallen</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'CLIMBED'")->fetch_row()[0]); ?> Climbed</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'FLOWN'")->fetch_row()[0]); ?> Flown</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'DOVE'")->fetch_row()[0]); ?> Dove</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'BY_MINECART'")->fetch_row()[0]); ?> By Minecart</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'BY_BOAT'")->fetch_row()[0]); ?> By Boat</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'BY_PIG'")->fetch_row()[0]); ?> By Pig</li>
          <li><?php echo ws_format_distance($mysqli->query("SELECT SUM(distance_count) FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = 'BY_HORSE'")->fetch_row()[0]); ?> By Horse</li>
        </ul>
      </div>
      <div>
        <h1>Materials</h1>
        <ul>
          <li><?php echo $mysqli->query("SELECT SUM(material_count) FROM ws_materials JOIN ws_material_events ON material_event = event_id WHERE event_name = 'BROKEN'")->fetch_row()[0]; ?> Broken</li>
          <li><?php echo $mysqli->query("SELECT SUM(material_count) FROM ws_materials JOIN ws_material_events ON material_event = event_id WHERE event_name = 'PLACED'")->fetch_row()[0]; ?> Placed</li>
          <li><?php echo $mysqli->query("SELECT SUM(material_count) FROM ws_materials JOIN ws_material_events ON material_event = event_id WHERE event_name = 'CRAFTED'")->fetch_row()[0]; ?> Crafted</li>
          <li><?php echo $mysqli->query("SELECT SUM(material_count) FROM ws_materials JOIN ws_material_events ON material_event = event_id WHERE event_name = 'DROPPED'")->fetch_row()[0]; ?> Dropped</li>
          <li><?php echo $mysqli->query("SELECT SUM(material_count) FROM ws_materials JOIN ws_material_events ON material_event = event_id WHERE event_name = 'PICKED_UP'")->fetch_row()[0]; ?> Picked Up</li>
        </ul>
      </div>
    </section>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>
