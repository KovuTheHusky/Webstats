<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

$time = 'day';
if (isset($_GET['time']))
	$time = $_GET['time'];
switch ($time) {
	case 'day':
		$where = 'distance_time > \'' . date('Y-m-d H:i:s', time() - 3600 * 24) . '\'';
		$group = 'HOUR(t)';
		$entries = 24;
		$label = 'F d, Y G:i:s';
		$key_format = 'Y-m-d H:00:00';
		$key_constant = 3600;
		break;
	case 'week':
		$where = 'distance_time > \'' . date('Y-m-d 00:00:00', time() - 3600 * 24 * 6) . '\'';
		$group = 'DAY(t)';
		$entries = 7;
		$label = 'F d, Y';
		$key_format = 'Y-m-d';
		$key_constant = 3600 * 24;
		break;
	case 'month':
		$where = 'distance_time > \'' . date('Y-m-d H:i:s', time() - 3600 * 24 * 30) . '\'';
		$group = 'DAY(t)';
		$entries = 30;
		$label = 'F d, Y';
		$key_format = 'Y-m-d';
		$key_constant = 3600 * 24;
		break;
	case 'year':
		$where = 'distance_time > \'' . date('Y-m-d H:i:s', time() - 3600 * 24 * 365) . '\'';
		$group = 'MONTH(t)';
		$entries = 12;
		$label = 'F d, Y';
		$key_format = 'Y-m-01';
		$key_constant = 3600 * 24 * 30; // TODO: This won't work on edge cases, do months manually?
		break;
	case 'all':
		$where = '1';
		$group = 'MONTH(t)';
		$entries = 24;
		$label = 'F d, Y';
		$key_format = 'Y-m-01';
		$key_constant = 3600 * 24 * 30; // TODO: This won't work on edge cases, do months manually?
		break;
	default:
		exit('error');
}

$res = $mysqli->query("SELECT type_name AS e, SUM(distance_count) AS c FROM ws_distances JOIN ws_distance_types ON type_id = distance_type WHERE {$where} GROUP BY distance_type ORDER BY c DESC");
for ($i = 0; $row = $res->fetch_assoc(); ++$i) {
	$donut[$i]['event'] = $row['e'];
	$donut[$i]['count'] = (int)$row['c'];
	$event[] = strtolower($row['e']); // temp
}

if (!empty($donut)) {

	for ($i = 0; $i < $entries; ++$i) {
		$key = date($key_format, time() - $key_constant * $i);
		$line[$key]['time'] = new DateTime($key);
		foreach ($event as $e)
			$line[$key][$e] = 0;
	}
	foreach ($event as $e) {
		$res = $mysqli->query("SELECT SUM(distance_count) as c, distance_time as t FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE type_name = '{$e}' AND {$where} GROUP BY {$group} ORDER BY t ASC");
		while ($row = $res->fetch_assoc()) {
			$line[date($key_format, (new DateTime($row['t']))->getTimestamp())][$e] = (int)$row['c'];
		}
	}
}

$line = array_values(array_reverse($line));

// echo '<pre>';
// print_r($line);
// echo '</pre>';
// exit();

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
<?php for ($i = 0; $i < count($donut); ++$i) { ?>
				,['<?php echo ucwords(strtolower(str_replace('_', ' ', $donut[$i]['event']))); ?>', <?php echo $donut[$i]['count']; ?>]
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
				['Time'<?php foreach ($event as $e) { ?>, '<?php echo ucwords(str_replace('_', ' ', $e)); ?>'<?php } ?>]
<?php for ($i = 0; $i < count($line); ++$i) { ?>
				,[new Date('<?php echo date($label, $line[$i]['time']->getTimestamp()); ?>')<?php foreach ($event as $e) { ?>, <?php echo $line[$i][$e]; ?><?php } ?>]
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
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_header.php'; ?>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <nav class="small">
    <ul>
      <li>View data from last:</li>
      <li<?php if ($time == 'day') echo ' class="selected"'; ?>><a href="/distances/day">Day</a></li>
      <li<?php if ($time == 'week') echo ' class="selected"'; ?>><a href="/distances/week">Week</a></li>
      <li<?php if ($time == 'month') echo ' class="selected"'; ?>><a href="/distances/month">Month</a></li>
      <li<?php if ($time == 'year') echo ' class="selected"'; ?>><a href="/distances/year">Year</a></li>
      <li<?php if ($time == 'all') echo ' class="selected"'; ?>><a href="/distances/all">All</a></li>
    </ul>
  </nav>
  <article>
    <div id="donut" class="chart"></div>
    <div id="line" class="chart"></div>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>
