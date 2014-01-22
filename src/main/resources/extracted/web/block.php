<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';
$block = strtolower($_GET['block']);

if (!$mysqli->query("SELECT COUNT(0) FROM ws_material_types WHERE type_name = '{$block}'")->fetch_row()[0])
	exit(header('Location: http' . (!empty($_SERVER['HTTPS']) ? 's' : '') . '://' . $_SERVER['SERVER_NAME'] . ':' . $_SERVER['SERVER_PORT'] . '/error/100'));

$res = $mysqli->query("SELECT event_name AS e, COUNT(0) AS c FROM ws_materials JOIN ws_material_events ON event_id = material_event JOIN ws_material_types ON type_id = material_type WHERE type_name = '{$block}' AND event_name != 'BLOCK_PHYSICS' GROUP BY material_event ORDER BY c DESC");
while ($row = $res->fetch_assoc()) {
	$event_raw[] = $row['e'];
	$event[] = ucwords(strtolower(str_replace('_', ' ', $row['e'])));
	$count[] = $row['c'];
}
if (!empty($event_raw)) {
	for ($i = 0; $i < 24; ++$i)
		foreach ($event_raw as $e)
			$over_time[$i][$e] = 0;
	foreach ($event_raw as $e) {
		$res = $mysqli->query("SELECT COUNT(0) as c, material_time as t FROM ws_materials JOIN ws_material_events ON material_event = event_id JOIN ws_material_types ON material_type = type_id WHERE type_name = '{$block}' AND event_name = '{$e}' GROUP BY HOUR(t)");
		while ($row = $res->fetch_assoc()) {
			$over_time[date('G', (new DateTime($row['t']))->getTimestamp())][$e] = $row['c'];
		}
	}
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="icon" href="/favicon.png" type="image/png">
<link rel="stylesheet" href="/assets/styles/global.css" type="text/css" />
<title><?php echo ucwords(strtolower($block)); ?> | Webstats</title>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
	google.load("visualization", "1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);
	function drawChart() {
		var data = google.visualization.arrayToDataTable([
				['Event', 'Count']
<?php for ($i = 0; $i < count($event); ++$i) { ?>
				,['<?php echo $event[$i]; ?>', <?php echo $count[$i]; ?>]
<?php } ?>
		]);
		var options = {
				legend: { position: 'right', alignment: 'center' },
				pieHole: 0.5
		};
		var chart = new google.visualization.PieChart(document.getElementById('donut'));
		chart.draw(data, options);
	}
</script>
<script type="text/javascript">
	google.load("visualization", "1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);
	function drawChart() {
		var data = google.visualization.arrayToDataTable([
				['Time'<?php foreach ($event as $e) { ?>, '<?php echo $e; ?>'<?php } ?>]
<?php for ($i = 0; $i < 24; ++$i) { ?>
				,['<?php echo $i; ?>:00'<?php foreach ($event_raw as $e) { ?>, <?php echo $over_time[$i][$e]; ?><?php } ?>]
<?php } ?>
        ]);
		var options = {
				legend: { position: 'bottom', alignment: 'center' }
		};
		var chart = new google.visualization.LineChart(document.getElementById('line'));
		chart.draw(data, options);
	}
</script>
</head>
<body>
  <header class="<?php echo $online ? 'green' : 'red'; ?>"> </header>
  <nav>
    <ul class="box">
      <li>Text</li>
    </ul>
  </nav>
  <article>
    <div class="block">
      <img src="/assets/images/materials/<?php echo $block; ?>.png" alt="<?php echo ucwords($block); ?>" />
    </div>
    <div id="donut" class="chart"></div>
    <div id="line" class="chart"></div>
  </article>
  <footer>
    Data from <a href="http://dev.bukkit.org/bukkit-mods/webstats">Webstats</a> ${project.version} by <a href="http://codeski.com">Codeski</a>
  </footer>
</body>
</html>