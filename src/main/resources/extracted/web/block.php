<?php

$block = strtolower($_GET['b']);

$mysqli = new mysqli('127.0.0.1', 'root', '1234', 'webstats_test', 3306);
if ($mysqli->connect_errno) {
	echo "Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error;
}

$res = $mysqli->query("SELECT event_name AS e, COUNT(0) AS c FROM ws_materials JOIN ws_material_events ON event_id = material_event JOIN ws_material_types ON type_id = material_type WHERE type_name = '{$block}' AND event_name != 'BLOCK_PHYSICS' GROUP BY material_event ORDER BY c DESC");
while ($row = $res->fetch_assoc()) {
	$event_raw[] = $row['e'];
	$event[] = ucwords(strtolower(str_replace('_', ' ', $event_raw[count($event_raw) - 1])));
	$count[] = $row['c'];
}

for ($i = 0; $i < 24; ++$i)
	foreach ($event_raw as $e)
		$over_time[$i][$e] = 0;

foreach ($event_raw as $e) {

	$res = $mysqli->query("SELECT COUNT(0) as c, material_time as t FROM ws_materials JOIN ws_material_events ON material_event = event_id JOIN ws_material_types ON material_type = type_id WHERE type_name = '{$block}' AND event_name = '{$e}' GROUP BY HOUR(t)");
	while ($row = $res->fetch_assoc()) {
		$over_time[date('G', (new DateTime($row['t']))->getTimestamp())][$e] = $row['c'];
	}
}

// echo '<pre>';
// print_r($over_time);
// exit();



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
          <?php

          for ($i = 0; $i < count($event); ++$i) {
			  	echo ",['{$event[$i]}', {$count[$i]}]";
          }

          ?>
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
          ['Time'<?php

          foreach ($event as $e) {
 echo ", '{$e}'";
}


          ?>]
          <?php

         for ($i = 0; $i < 24; ++$i) {
         	 echo ",['{$i}:00'";
			 foreach ($event_raw as $e) {
			    echo ", " . $over_time[$i][$e];
			 }
			 echo "]";
          }

          ?>
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
<header class="<?php echo $stats['startup'] > $stats['shutdown'] ? 'green' : 'red'; ?>">
</header>
<nav>
  <ul class="box">
    <li>Text</li>
  </ul>
</nav>
<article>
  <div class="block">
    <img src="/assets/images/<?php echo $block; ?>.png" alt="<?php echo ucwords($block); ?>" />
  </div>
  <div id="events" class="chart"></div>
  <div id="overtime" class="chart"></div>
</article>
<footer>
  Data from <a href="http://dev.bukkit.org/bukkit-mods/webstats">Webstats</a> ${project.version} by <a href="http://codeski.com">Codeski</a>
</footer>
</body>
</html>