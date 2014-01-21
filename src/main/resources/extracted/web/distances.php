<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

$res = $mysqli->query("SELECT type_name AS e, SUM(distance_count) AS c FROM ws_distances JOIN ws_distance_types ON type_id = distance_type GROUP BY distance_type ORDER BY c DESC");
while ($row = $res->fetch_assoc()) {
	$event_raw[] = $row['e'];
	$event[] = ucwords(strtolower(str_replace('_', ' ', $row['e'])));
	$count[] = (int)$row['c'];
}
if (!empty($event_raw)) {
	for ($i = 0; $i < 24; ++$i)
	foreach ($event_raw as $e)
		$over_time[$i][$e] = 0;
	foreach ($event_raw as $e) {
		$res = $mysqli->query("SELECT SUM(distance_count) as c, distance_time as t FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = '{$e}' GROUP BY HOUR(t)");
		while ($row = $res->fetch_assoc()) {
			$over_time[date('G', (new DateTime($row['t']))->getTimestamp())][$e] = (int)$row['c'];
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
<title>Distances | Webstats</title>
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
				title: 'Events In Total',
				legend: { position: 'right', alignment: 'center' },
				pieHole: 0.5
		};
		var chart = new google.visualization.PieChart(document.getElementById('events'));
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
				title: 'Events Over Time',
				legend: { position: 'bottom', alignment: 'center' }
		};
		var chart = new google.visualization.LineChart(document.getElementById('overtime'));
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
    <div id="events" class="chart"></div>
    <div id="overtime" class="chart"></div>
  </article>
  <footer>
    Data from <a href="http://dev.bukkit.org/bukkit-mods/webstats">Webstats</a> ${project.version} by <a href="http://codeski.com">Codeski</a>
  </footer>
</body>
</html>