<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

// Get the first and last rows
$res = $mysqli->query("SELECT material_time FROM ws_materials ORDER BY material_time ASC LIMIT 1");

// Make sure we have at least one row
if (!$res->num_rows)
	exit('no data'); //ws_error(101);

$first = new DateTime($res->fetch_row()[0]);
$res = $mysqli->query("SELECT material_time FROM ws_materials ORDER BY material_time DESC LIMIT 1");
$last = new DateTime($res->fetch_row()[0]);

// Start, end, and intervals for times
$time_end = (new DateTime())->modify('+1 second');
$dt = array(
		'day' => array(
				'start' => (new DateTime())->sub(new DateInterval('PT23H')),
				'interval' => 'HOUR'
		),
		'week' => array(
				'start' => (new DateTime())->sub(new DateInterval('P6D')),
				'interval' => 'DAY'
		),
		'month' => array(
				'start' => (new DateTime())->sub(new DateInterval('P29D')),
				'interval' => 'DAY'
		),
		'year' => array(
				'start' => (new DateTime())->sub(new DateInterval('P11M')),
				'interval' => 'MONTH'
		),
		'all' => array(
				'start' => $first,
				'interval' => 'MONTH'
		)
);

// Make sure all is at least one year
if ($dt['all']['start'] > $dt['year']['start'])
	$dt['all']['start'] = $dt['year']['start'];

// Find out valid periods
foreach ($dt as $key => $value)
	$valid[$key] = $last > $value['start'];

// Find out which charts to draw
$by = isset($_GET['by']) ? $_GET['by'] : 'type';

// Find out what time period
if (isset($_GET['time']))
	$desired_time = $_GET['time'];
foreach ($valid as $key => $value) {
	if ($value)
		$time = $key;
	if (!isset($desired_time) && $value || isset($desired_time) && $desired_time == $key)
		break;
}

// If different than specified, redirect
if (isset($_GET['time']) && $desired_time != $time)
	ws_redirect("/materials/{$time}/{$by}");

// More variable specific data
switch($dt[$time]['interval']) {
	case 'HOUR':
		$array_key = 'Y-m-d H:00:00';
		$time_step = new DateInterval('PT1H');
		$time_range = new DatePeriod($dt[$time]['start'], $time_step, $time_end);
		break;
	case 'DAY':
		$array_key = 'Y-m-d 00:00:00';
		$time_step = new DateInterval('P1D');
		$time_range = new DatePeriod($dt[$time]['start'], $time_step, $time_end);
		break;
	case 'MONTH':
		$array_key = 'Y-m-01 00:00:00';
		$time_step = new DateInterval('P1M');
		$time_range = new DatePeriod($dt[$time]['start'], $time_step, $time_end);
		break;
}

// Are they views times or dates
$time_format = $dt[$time]['interval'] == 'HOUR' ? 'F d, Y G:00:00' : 'F d, Y';

// Get the data for the donut chart
if ($by == 'event')
	$donut = $mysqli->query("SELECT event_name, SUM(material_count) FROM ws_materials JOIN ws_material_events ON event_id = material_event WHERE material_time >= '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY material_event ORDER BY SUM(material_count) DESC")->fetch_all();
else if ($by == 'type')
	$donut = $mysqli->query("SELECT type_name, SUM(material_count) FROM ws_materials JOIN ws_material_types ON type_id = material_type WHERE material_time >= '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY material_type ORDER BY SUM(material_count) DESC")->fetch_all();
else if ($by == 'player')
	$donut = $mysqli->query("SELECT player_name, SUM(material_count) FROM ws_materials JOIN ws_players ON player_id = material_player WHERE material_time >= '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY player_name ORDER BY SUM(material_count) DESC")->fetch_all();

// Get the data for the line chart if we need it
foreach ($time_range as $cur) {
	$key = $cur->format($array_key);
	$line[$key]['time'] = $cur;
	for ($j = 0; $j < count($donut); ++$j)
		$line[$key][$donut[$j][0]] = 0;
}
if ($by == 'event')
	$res = $mysqli->query("SELECT event_name AS 'event', SUM(material_count) AS 'count', material_time AS 'time' FROM ws_materials JOIN ws_material_events ON event_id = material_event WHERE material_time > '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY event_name, {$dt[$time]['interval']}(material_time) ORDER BY material_time ASC");
else if ($by == 'type')
	$res = $mysqli->query("SELECT type_name AS 'event', SUM(material_count) AS 'count', material_time AS 'time' FROM ws_materials JOIN ws_material_types ON material_type = type_id WHERE material_time > '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY type_name, {$dt[$time]['interval']}(material_time) ORDER BY material_time ASC");
else if ($by == 'player')
	$res = $mysqli->query("SELECT player_name AS 'event', SUM(material_count) AS 'count', material_time AS 'time' FROM ws_materials JOIN ws_players ON player_id = material_player WHERE material_time > '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY player_name, {$dt[$time]['interval']}(material_time) ORDER BY material_time ASC");
if ($res->num_rows < 1)
	$line = array();
while ($row = $res->fetch_assoc()) {
	$line[date($array_key, (new DateTime($row['time']))->getTimestamp())][$row['event']] = $row['count'];
}

$line = array_values($line);

// echo '<pre>';
// exit(print_r($line));

?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="icon" href="/favicon.png" type="image/png">
<link rel="stylesheet" href="/assets/styles/global.css" type="text/css" />
<title>Materials | Webstats</title>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
	google.load("visualization", "1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);
	function drawChart() {
		var data = google.visualization.arrayToDataTable([
				['Event', 'Count']
<?php for ($i = 0; $i < count($donut); ++$i) { ?>
				,['<?php echo $by == 'player' ? $donut[$i][0] : ws_enum_decode($donut[$i][0]); ?>', <?php echo $donut[$i][1]; ?>]
<?php } ?>
		]);
		var options = {
				title: 'Materials, Total',
				legend: { position: 'right', alignment: 'center' },
				pieHole: 0.5
		};
		var chart = new google.visualization.PieChart(document.getElementById('donut'));
		chart.draw(data, options);
		$("text", "#donut").click(function() {
			window.location.href = '<?php echo 'http' . (!empty($_SERVER['HTTPS']) ? 's' : '') . '://' . $_SERVER['SERVER_NAME'] . ':' . $_SERVER['SERVER_PORT']; ?>/material/' + $(this).text().toLowerCase().replace(/ /g, '_');
		});
	}
</script>
<script type="text/javascript">
	google.load("visualization", "1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);
	function drawChart() {
		var data = google.visualization.arrayToDataTable([
				['Time'<?php for ($i = 0; $i < count($donut); ++$i) { ?>, '<?php echo $by == 'player' ? $donut[$i][0] : ws_enum_decode($donut[$i][0]); ?>'<?php } ?>]
<?php for ($i = 0; $i < count($line); ++$i) { ?>
				,[new Date('<?php echo $line[$i]['time']->format($time_format); ?>')<?php for ($j = 0; $j < count($donut); ++$j) { ?>, <?php echo $line[$i][$donut[$j][0]]; ?><?php } ?>]
<?php } ?>
        ]);
		var options = {
				title: 'Materials By Time',
				legend: { position: 'bottom', alignment: 'center' }
		};
		var chart = new google.visualization.LineChart(document.getElementById('line'));
		chart.draw(data, options);
		$("text", "#line").click(function() {
			window.location.href = '<?php echo 'http' . (!empty($_SERVER['HTTPS']) ? 's' : '') . '://' . $_SERVER['SERVER_NAME'] . ':' . $_SERVER['SERVER_PORT']; ?>/material/' + $(this).text().toLowerCase().replace(/ /g, '_');
		});
	}
</script>
<script type="text/javascript" src="http://code.jquery.com/jquery.min.js"></script>
</head>
<body>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_header.php'; ?>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <nav class="small">
    <ul>
      <li>View data from last:</li>
      <li<?php if ($time == 'day') echo ' class="selected"'; ?>><?php if ($valid['day']) { ?><a href="/materials/day/<?php echo $by; ?>">Day</a><?php } else { ?><span style="text-decoration: line-through;">Day</span><?php } ?></li>
      <li<?php if ($time == 'week') echo ' class="selected"'; ?>><?php if ($valid['week']) { ?><a href="/materials/week/<?php echo $by; ?>">Week</a><?php } else { ?><span style="text-decoration: line-through;">Week</span><?php } ?></li>
      <li<?php if ($time == 'month') echo ' class="selected"'; ?>><?php if ($valid['month']) { ?><a href="/materials/month/<?php echo $by; ?>">Month</a><?php } else { ?><span style="text-decoration: line-through;">Month</span><?php } ?></li>
      <li<?php if ($time == 'year') echo ' class="selected"'; ?>><?php if ($valid['year']) { ?><a href="/materials/year/<?php echo $by; ?>">Year</a><?php } else { ?><span style="text-decoration: line-through;">Year</span><?php } ?></li>
      <li<?php if ($time == 'all') echo ' class="selected"'; ?>><a href="/materials/all/<?php echo $by; ?>">All</a></li>
    </ul>
    <ul>
      <li>By:</li>
      <li<?php if ($by == 'event') echo ' class="selected"'; ?>><a href="/materials/<?php echo $time; ?>/event">Event</a></li>
      <li<?php if ($by == 'player') echo ' class="selected"'; ?>><a href="/materials/<?php echo $time; ?>/player">Player</a></li>
      <li<?php if ($by == 'type') echo ' class="selected"'; ?>><a href="/materials/<?php echo $time; ?>/type">Type</a></li>
    </ul>
    <ul style="display: none;">
      <li style="cursor: pointer;" onclick="javascript:document.getElementById('face_roll').style.display = 'block';">More...</li>
    </ul>
  </nav>
  <article>
    <div class="face_roll small" id="face_roll" style="display: none;">
      <ul>
        <li>Limit to players:</li>
        <li><a href="#"><img src="/uploads/kovuthehusky.png" /></a></li>
        <li><a href="#"><img src="/uploads/kuperfox.png" /></a></li>
        <li class="text"><a href="#"><span style="display: block; -webkit-transform: rotate(45deg); -ms-transform: rotate(45deg); transform: rotate(45deg); height: 30px;">&#x2718;</span></a></li>
        <li class="text"><a href="#">&#x2717;</a></li>
      </ul>
    </div>
    <div id="donut" class="linked chart"></div>
    <div id="line" class="linked chart"></div>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>
