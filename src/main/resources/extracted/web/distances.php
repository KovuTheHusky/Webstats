<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

// If there is no data, error
$res = $mysqli->query("SELECT distance_time FROM ws_distances ORDER BY distance_time DESC LIMIT 1");
if (!$res->num_rows)
	ws_error(102);

// Figure out all the variables

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
				'start' => (new DateTime($res->fetch_row()[0])),
				'interval' => 'MONTH'
		)
);

// Make sure all is at least one year
if ($dt['all']['start'] > $dt['year']['start'])
	$dt['all']['start'] = $dt['year']['start'];

// Find out valid periods
$recent = new DateTime($res->fetch_row()[0]);
foreach ($dt as $key => $value)
	$valid[$key] = $recent > $dt[$key];

// Find out which charts to draw
$chart = isset($_GET['chart']) ? $_GET['chart'] : 'all';

// Find out what time period
$desired_time = isset($_GET['time']) ? $_GET['time'] : 'day';
foreach ($valid as $key => $value) {
	if ($valid[$key])
		$time = $key;
	if ($desired_time == $key)
		break;
}

// If different than specified, redirect
if ($desired_time != $time && isset($_GET['time']))
	ws_redirect("/distances/{$time}/{$chart}");

switch($dt[$time]['interval']) {
	case 'HOUR':
		$array_key = 'Y-m-d H:00:00';
		$time_step = new DateInterval('PT1H');
		$time_count = $time_end->diff($dt[$time]['start'])->format('%h') + 1;
		$time_range = new DatePeriod($dt[$time]['start'], $time_step, $time_end);
		break;
	case 'DAY':
		$array_key = 'Y-m-d 00:00:00';
		$time_step = new DateInterval('P1D');
		$time_count = $time_end->diff($dt[$time]['start'])->format('%d') + 1;
		$time_range = new DatePeriod($dt[$time]['start'], $time_step, $time_end);
		break;
	case 'MONTH':
		$array_key = 'Y-m-01 00:00:00';
		$time_step = new DateInterval('P1M');
		$time_count = $time_end->diff($dt[$time]['start'])->format('%m') + 1;
		$time_range = new DatePeriod($dt[$time]['start'], $time_step, $time_end);
		break;
}

$time_format = $dt[$time]['interval'] == 'HOUR' ? 'F d, Y G:00:00' : 'F d, Y';

// Get the data for the donut chart
$donut = $mysqli->query("SELECT type_name, ROUND(SUM(distance_count)) FROM ws_distances JOIN ws_distance_types ON type_id = distance_type WHERE distance_time >= '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY distance_type ORDER BY SUM(distance_count) DESC")->fetch_all();

// Get the data for the line chart if we need it
if ($chart == 'line' || $chart == 'all') {

	foreach ($time_range as $cur) {
		$key = $cur->format($array_key);
		$line[$key]['time'] = $cur;
		for ($j = 0; $j < count($donut); ++$j)
			$line[$key][$donut[$j][0]] = 0;
	}

	$res = $mysqli->query("SELECT type_name AS 'event', SUM(distance_count) AS 'count', distance_time AS 'time' FROM ws_distances JOIN ws_distance_types ON distance_type = type_id WHERE distance_time > '{$dt[$time]['start']->format('Y-m-d H:i:s')}' GROUP BY type_name, {$dt[$time]['interval']}(distance_time) ORDER BY distance_time ASC");
	if ($res->num_rows < 1)
		$line = array();
	while ($row = $res->fetch_assoc()) {
		$line[date($array_key, (new DateTime($row['time']))->getTimestamp())][$row['event']] = round($row['count']);
	}

	$line = array_values($line);

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
<?php for ($i = 0; $i < count($donut); ++$i) { ?>
				,['<?php echo ucwords(strtolower(str_replace('_', ' ', $donut[$i][0]))); ?>', <?php echo $donut[$i][1]; ?>]
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

<?php if ($chart == 'line' || $chart == 'all') { ?>

<script type="text/javascript">
	google.load("visualization", "1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);
	function drawChart() {
		var data = google.visualization.arrayToDataTable([
				['Time'<?php for ($i = 0; $i < count($donut); ++$i) { ?>, '<?php echo ucwords(strtolower(str_replace('_', ' ', $donut[$i][0]))); ?>'<?php } ?>]
<?php for ($i = 0; $i < count($line); ++$i) { ?>
				,[new Date('<?php echo $line[$i]['time']->format($time_format); ?>')<?php for ($j = 0; $j < count($donut); ++$j) { ?>, <?php echo $line[$i][$donut[$j][0]]; ?><?php } ?>]
<?php } ?>
        ]);
		var options = {
				legend: { position: 'bottom', alignment: 'center' }
		};
		var chart = new google.visualization.LineChart(document.getElementById('line'));
		chart.draw(data, options);
	}
</script>

<?php } ?>

</head>
<body>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_header.php'; ?>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <nav class="small">
    <ul>
      <li>View data from last:</li>
      <li<?php if ($time == 'day') echo ' class="selected"'; ?>><?php if ($valid['day']) { ?><a href="/distances/day/<?php echo $chart; ?>">Day</a><?php } else { ?><span style="text-decoration: line-through;">Day</span><?php } ?></li>
      <li<?php if ($time == 'week') echo ' class="selected"'; ?>><?php if ($valid['week']) { ?><a href="/distances/week/<?php echo $chart; ?>">Week</a><?php } else { ?><span style="text-decoration: line-through;">Week</span><?php } ?></li>
      <li<?php if ($time == 'month') echo ' class="selected"'; ?>><?php if ($valid['month']) { ?><a href="/distances/month/<?php echo $chart; ?>">Month</a><?php } else { ?><span style="text-decoration: line-through;">Month</span><?php } ?></li>
      <li<?php if ($time == 'year') echo ' class="selected"'; ?>><?php if ($valid['year']) { ?><a href="/distances/year/<?php echo $chart; ?>">Year</a><?php } else { ?><span style="text-decoration: line-through;">Year</span><?php } ?></li>
      <li<?php if ($time == 'all') echo ' class="selected"'; ?>><a href="/distances/all/<?php echo $chart; ?>">All</a></li>
    </ul>
    <ul>
      <li>Chart type:</li>
      <li<?php if ($chart == 'donut') echo ' class="selected"'; ?>><a href="/distances/<?php echo $time; ?>/donut">Donut</a></li>
      <li<?php if ($chart == 'line') echo ' class="selected"'; ?>><a href="/distances/<?php echo $time; ?>/line">Line</a></li>
      <li<?php if ($chart == 'all') echo ' class="selected"'; ?>><a href="/distances/<?php echo $time; ?>/all">All</a></li>
    </ul>
  </nav>
  <article>
<?php if ($chart == 'donut' || $chart == 'all') { ?>
    <div id="donut" class="chart"></div>
<?php } ?>
<?php if ($chart == 'line' || $chart == 'all') { ?>
    <div id="line" class="chart"></div>
<?php } ?>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>
