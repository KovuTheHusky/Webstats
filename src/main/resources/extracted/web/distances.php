<?php

require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

// Get the first and last rows
$res = $mysqli->query("SELECT distance_time FROM ws_distances ORDER BY distance_time ASC LIMIT 1");

// Make sure we have at least one row
if (!$res->num_rows)
	ws_error(102);

$first = new DateTime($res->fetch_row()[0]);
$res = $mysqli->query("SELECT distance_time FROM ws_distances ORDER BY distance_time DESC LIMIT 1");
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
	ws_redirect("/distances/{$time}/{$by}");

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
if ($by == 'type')
	$donut = $mysqli->query("SELECT type_name, ROUND(SUM(distance_count)) FROM ws_distances JOIN ws_distance_types ON type_id = distance_type WHERE distance_time >= '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY distance_type ORDER BY SUM(distance_count) DESC")->fetch_all();
else if ($by == 'player')
	$donut = $mysqli->query("SELECT player_name, ROUND(SUM(distance_count)) FROM ws_distances JOIN ws_players ON player_id = distance_player WHERE distance_time >= '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY player_name ORDER BY SUM(distance_count) DESC")->fetch_all();

// Get the data for the line chart if we need it
foreach ($time_range as $cur) {
	$key = $cur->format($array_key);
	$line[$key]['time'] = $cur;
	for ($j = 0; $j < count($donut); ++$j)
		$line[$key][$donut[$j][0]] = 0;
}
if ($by == 'type')
	$res = $mysqli->query("SELECT type_name AS 'event', ROUND(SUM(distance_count)) AS 'count', distance_time AS 'time' FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE distance_time > '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY type_name, {$dt[$time]['interval']}(distance_time) ORDER BY distance_time ASC");
else if ($by == 'player')
$res = $mysqli->query("SELECT player_name AS 'event', ROUND(SUM(distance_count)) AS 'count', distance_time AS 'time' FROM ws_distances JOIN ws_players ON player_id = distance_player WHERE distance_time > '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY player_name, {$dt[$time]['interval']}(distance_time) ORDER BY distance_time ASC");
if ($res->num_rows < 1)
	$line = array();
while ($row = $res->fetch_assoc()) {
	$line[date($array_key, (new DateTime($row['time']))->getTimestamp())][$row['event']] = $row['count'];
}

$line = array_values($line);

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
				table.chart.setSelection([]);
			} else {
				table.chart.setSelection(sel);
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
				table.chart.setSelection([]);
			} else {
				sel[0].row = sel[0].column - 1;
				sel[0].column = null;
				donut.chart.setSelection(sel);
				table.chart.setSelection(sel);
			}
		});
	}
</script>
<script type="text/javascript">
	var table = {
			data: null,
			options: {
					showRowNumber: true,
					allowHtml: true
			},
			chart: null
	};
	google.load("visualization", "1", {packages:["table"]});
	google.setOnLoadCallback(prepareTable);
	function prepareTable() {
		table.data = new google.visualization.DataTable();
		table.data.addColumn('string', '<?php echo ucwords($by); ?>');
		table.data.addColumn('number', 'Count');
		table.data.addRows([
<?php for ($i = 0; $i < count($donut); ++$i) { ?>
<?php if ($by == 'player') { ?>
				<?php if ($i != 0) echo ','; ?>['<a href="<?php echo '/player/' . strtolower($donut[$i][0]); ?>" style="height: 24px; padding: 0; display: inline-block;"><img src="/uploads/<?php echo strtolower($donut[$i][0]); ?>.png" style="vertical-align: middle; height: 24px; padding: 0; margin-right: 2px;" /><span style="padding: 1px 0 0 0; display: inline-block; height: 24px; line-height: 24px;"><?php echo $by == 'player' ? $donut[$i][0] : ws_enum_decode($donut[$i][0]); ?></span></a>', <?php echo $donut[$i][1]; ?>]
<?php } else if ($by == 'type') { ?>
				<?php if ($i != 0) echo ','; ?>['<?php echo $by == 'player' ? $donut[$i][0] : ws_enum_decode($donut[$i][0]); ?>', <?php echo $donut[$i][1]; ?>]
<?php } ?>
<?php } ?>
		]);
		table.chart = new google.visualization.Table(document.getElementById('table'));
		drawTable();
	}
	function drawTable() {
		table.chart.draw(table.data, table.options);
		google.visualization.events.addListener(table.chart, 'select', function() {
			var sel = table.chart.getSelection();
			if (sel.length < 1) {
				donut.chart.setSelection([]);
				line.chart.setSelection([]);
			} else {
				donut.chart.setSelection(sel);
				sel[0].column = sel[0].row + 1;
				sel[0].row = null;
				line.chart.setSelection(sel);
			}
		});
	}
</script>
<script type="text/javascript" src="http://code.jquery.com/jquery.min.js"></script>
<script type="text/javascript">
	var colors = ["#3366cc","#dc3912","#ff9900","#109618","#990099","#0099c6","#dd4477","#66aa00","#b82e2e","#316395","#994499","#22aa99","#aaaa11","#6633cc","#e67300","#8b0707","#651067","#329262","#5574a6","#3b3eac","#b77322","#16d620","#b91383","#f4359e","#9c5935","#a9c413","#2a778d","#668d1c","#bea413","#0c5922","#743411"];
	var w = $(window).width();
	$(window).resize(function() {
		var nw = $(window).width();
		if (nw != w) {
			drawDonut();
			drawLine();
			drawTable();
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
      <li<?php if ($time == 'day') echo ' class="selected"'; ?>><?php if ($valid['day']) { ?><a href="/distances/day/<?php echo $by; ?>">Day</a><?php } else { ?><span style="text-decoration: line-through;">Day</span><?php } ?></li>
      <li<?php if ($time == 'week') echo ' class="selected"'; ?>><?php if ($valid['week']) { ?><a href="/distances/week/<?php echo $by; ?>">Week</a><?php } else { ?><span style="text-decoration: line-through;">Week</span><?php } ?></li>
      <li<?php if ($time == 'month') echo ' class="selected"'; ?>><?php if ($valid['month']) { ?><a href="/distances/month/<?php echo $by; ?>">Month</a><?php } else { ?><span style="text-decoration: line-through;">Month</span><?php } ?></li>
      <li<?php if ($time == 'year') echo ' class="selected"'; ?>><?php if ($valid['year']) { ?><a href="/distances/year/<?php echo $by; ?>">Year</a><?php } else { ?><span style="text-decoration: line-through;">Year</span><?php } ?></li>
      <li<?php if ($time == 'all') echo ' class="selected"'; ?>><a href="/distances/all/<?php echo $by; ?>">All</a></li>
    </ul>
    <ul>
      <li>By:</li>
      <li><span style="text-decoration: line-through;">Event</span></li>
      <li<?php if ($by == 'player') echo ' class="selected"'; ?>><a href="/distances/<?php echo $time; ?>/player">Player</a></li>
      <li<?php if ($by == 'type') echo ' class="selected"'; ?>><a href="/distances/<?php echo $time; ?>/type">Type</a></li>
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
    <div id="donut" class="chart"></div>
    <div id="line" class="chart"></div>
    <div id="table" class="table"></div>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>
