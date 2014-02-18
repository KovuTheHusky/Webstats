<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';
$material = strtolower($_GET['material']);
if (!$mysqli->query("SELECT COUNT(0) FROM ws_material_types WHERE type_name = '{$material}' LIMIT 1")->fetch_row()[0])
	ws_error(101);

// Get the first and last rows
$res = $mysqli->query("SELECT material_time FROM ws_materials JOIN ws_material_types ON material_type = type_id WHERE type_name = '{$material}' ORDER BY material_time ASC LIMIT 1");

// Make sure we have at least one row
if (!$res->num_rows)
	ws_error(102);

$first = new DateTime($res->fetch_row()[0]);
$res = $mysqli->query("SELECT material_time FROM ws_materials JOIN ws_material_types ON material_type = type_id WHERE type_name = '{$material}' ORDER BY material_time DESC LIMIT 1");
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
$by = isset($_GET['by']) ? $_GET['by'] : 'event';

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
	ws_redirect("/material/{$material}/{$time}/{$by}");

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
	$donut = $mysqli->query("SELECT event_name, SUM(material_count) FROM ws_materials JOIN ws_material_events ON event_id = material_event JOIN ws_material_types ON type_id = material_type WHERE material_time >= '{$dt[$time]['start']->format('Y-m-d H:i:s')}' AND type_name = '{$material}' GROUP BY material_event ORDER BY SUM(material_count) DESC")->fetch_all();
else if ($by == 'player')
	$donut = $mysqli->query("SELECT player_name, SUM(material_count) FROM ws_materials JOIN ws_players ON player_id = material_player JOIN ws_material_types ON type_id = material_type WHERE material_time >= '{$dt[$time]['start']->format('Y-m-d H:i:s')}' AND type_name = '{$material}' GROUP BY player_name ORDER BY SUM(material_count) DESC")->fetch_all();

// Get the data for the line chart if we need it
foreach ($time_range as $cur) {
	$key = $cur->format($array_key);
	$line[$key]['time'] = $cur;
	for ($j = 0; $j < count($donut); ++$j)
		$line[$key][$donut[$j][0]] = 0;
}
if ($by == 'event')
	$res = $mysqli->query("SELECT event_name AS 'event', SUM(material_count) AS 'count', material_time AS 'time' FROM ws_materials JOIN ws_material_events ON event_id = material_event JOIN ws_material_types ON type_id = material_type WHERE material_time > '{$dt[$time]['start']->format('Y-m-d H:i:s')}' AND type_name = '{$material}' GROUP BY event_name, {$dt[$time]['interval']}(material_time) ORDER BY material_time ASC");
else if ($by == 'player')
	$res = $mysqli->query("SELECT player_name AS 'event', SUM(material_count) AS 'count', material_time AS 'time' FROM ws_materials JOIN ws_players ON player_id = material_player JOIN ws_material_types ON type_id = material_type WHERE material_time > '{$dt[$time]['start']->format('Y-m-d H:i:s')}' AND type_name = '{$material}' GROUP BY player_name, {$dt[$time]['interval']}(material_time) ORDER BY material_time ASC");
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
<title><?php echo ws_enum_decode($material); ?> | Material | Webstats</title>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
	var donut = {
			data: null,
			options: {
					legend: 'none',
					pieHole: 0.5,
					sliceVisibilityThreshold: 0
			},
			chart: null
	};
	google.load("visualization", "1", {packages:["corechart"]});
	google.setOnLoadCallback(prepareDonut);
	function prepareDonut() {
		donut.data = google.visualization.arrayToDataTable([
				['Event', 'Count']
<?php for ($i = 0; $i < count($donut); ++$i) { ?>
				,['<?php echo $by == 'player' ? $donut[$i][0] : ws_enum_decode($donut[$i][0]); ?>', <?php echo $donut[$i][1]; ?>]
<?php } ?>
		]);
		donut.chart = new google.visualization.PieChart(document.getElementById('donut'));
		drawDonut();
	}
	function drawDonut() {
		donut.chart.draw(donut.data, donut.options);
		google.visualization.events.addListener(donut.chart, 'select', function() {
			var sel = donut.chart.getSelection();
			if (sel.length < 1) {
				line.chart.setSelection([]);
				// table.chart.setSelection([]);
			} else {
				// table.chart.setSelection(sel);
				sel[0].column = sel[0].row + 1;
				sel[0].row = null;
				line.chart.setSelection(sel);
			}
		});
	}
</script>
<script type="text/javascript">
	var line = {
			data: null,
			options: {
					legend: 'none'
			},
			chart: null
	};
	google.load("visualization", "1", {packages:["corechart"]});
	google.setOnLoadCallback(prepareLine);
	function prepareLine() {
		line.data = google.visualization.arrayToDataTable([
				['Time'<?php for ($i = 0; $i < count($donut); ++$i) { ?>, '<?php echo $by == 'player' ? $donut[$i][0] : ws_enum_decode($donut[$i][0]); ?>'<?php } ?>]
<?php for ($i = 0; $i < count($line); ++$i) { ?>
				,[new Date('<?php echo $line[$i]['time']->format($time_format); ?>')<?php for ($j = 0; $j < count($donut); ++$j) { ?>, <?php echo $line[$i][$donut[$j][0]]; ?><?php } ?>]
<?php } ?>
        ]);
		line.chart = new google.visualization.LineChart(document.getElementById('line'));
		drawLine();
	}
	function drawLine() {
		line.chart.draw(line.data, line.options);
		google.visualization.events.addListener(line.chart, 'select', function() {
			var sel = line.chart.getSelection();
			if (sel.length < 1) {
				donut.chart.setSelection([]);
				// table.chart.setSelection([]);
			} else {
				sel[0].row = sel[0].column - 1;
				sel[0].column = null;
				donut.chart.setSelection(sel);
				// table.chart.setSelection(sel);
			}
		});
	}
</script>
<script type="text/javascript" src="http://code.jquery.com/jquery.min.js"></script>
<script type="text/javascript">
	var w = $(window).width();
	$(window).resize(function() {
		var nw = $(window).width();
		if (nw != w) {
			drawDonut();
			drawLine();
		}
		w = nw;
	});
</script>
</head>
<body>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_header.php'; ?>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <nav class="small">
    <ul>
      <li>View data from last:</li>
      <li<?php if ($time == 'day') echo ' class="selected"'; ?>><?php if ($valid['day']) { ?><a href="/material/<?php echo $material; ?>/day/<?php echo $by; ?>">Day</a><?php } else { ?><span style="text-decoration: line-through;">Day</span><?php } ?></li>
      <li<?php if ($time == 'week') echo ' class="selected"'; ?>><?php if ($valid['week']) { ?><a href="/material/<?php echo $material; ?>/week/<?php echo $by; ?>">Week</a><?php } else { ?><span style="text-decoration: line-through;">Week</span><?php } ?></li>
      <li<?php if ($time == 'month') echo ' class="selected"'; ?>><?php if ($valid['month']) { ?><a href="/material/<?php echo $material; ?>/month/<?php echo $by; ?>">Month</a><?php } else { ?><span style="text-decoration: line-through;">Month</span><?php } ?></li>
      <li<?php if ($time == 'year') echo ' class="selected"'; ?>><?php if ($valid['year']) { ?><a href="/material/<?php echo $material; ?>/year/<?php echo $by; ?>">Year</a><?php } else { ?><span style="text-decoration: line-through;">Year</span><?php } ?></li>
      <li<?php if ($time == 'all') echo ' class="selected"'; ?>><a href="/material/<?php echo $material; ?>/all/<?php echo $by; ?>">All</a></li>
    </ul>
    <ul>
      <li>By:</li>
      <li<?php if ($by == 'event') echo ' class="selected"'; ?>><a href="/material/<?php echo $material; ?>/<?php echo $time; ?>/event">Event</a></li>
      <li<?php if ($by == 'player') echo ' class="selected"'; ?>><a href="/material/<?php echo $material; ?>/<?php echo $time; ?>/player">Player</a></li>
      <li><span style="text-decoration: line-through;">Type</span></li>
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
        <li class="text"><a href="#"><span style="display: block; -webkit-transform: rotate(45deg); -ms-transform: rotate(45deg); transform: rotate(45deg); height: 30px;">&#x2718;</span></a></li>
        <li class="text"><a href="#">&#x2717;</a></li>
      </ul>
    </div>
    <div class="material full">
      <img src="/assets/images/materials/<?php echo $material; ?>.png" alt="<?php echo ws_enum_decode($material); ?>" />
    </div>
    <div id="donut" class="chart"></div>
    <div id="line" class="chart"></div>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>
