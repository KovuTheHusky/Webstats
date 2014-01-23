<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

$res = $mysqli->query("SELECT type_name FROM ws_material_types ORDER BY type_name ASC");
while ($row = $res->fetch_assoc()) {
	$material[] = $row['type_name'];
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="icon" href="/favicon.png" type="image/png">
<link rel="stylesheet" href="/assets/styles/global.css" type="text/css" />
<title>Materials | Webstats</title>
</head>
<body>
  <header class="<?php echo $online ? 'green' : 'red'; ?>"></header>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <article>
<?php foreach ($material as $m) { ?>
    <div style="width: 150px; height: 150px; display: inline-block; border: none; vertical-align: top; padding: 0; margin-bottom: 0; line-height: 150px;">
<?php if (!file_exists($_SERVER['DOCUMENT_ROOT'] . '/assets/images/materials/' . strtolower($m) . '.png')) { ?>
      <a href="/material/<?php echo strtolower($m); ?>" style="display: block; width: 150px; height: 150px;"><?php echo ucwords(strtolower(str_replace('_', ' ', $m))); ?></a>
<?php } else { ?>
      <a href="/material/<?php echo strtolower($m); ?>" style="display: block; width: 150px; height: 150px;"><img src="/assets/images/materials/<?php echo strtolower($m); ?>.png" alt="<?php echo ucwords(strtolower(str_replace('_', ' ', $m))); ?>" style="max-width: 150px; max-height: 150px; vertical-align: middle; margin: auto;" /></a>
<?php } ?>
    </div>
<?php } ?>
  </article>
  <footer style="margin-top: 10px;">
    Data from <a href="http://dev.bukkit.org/bukkit-mods/webstats">Webstats</a> ${project.version} by <a href="http://codeski.com">Codeski</a>
  </footer>
</body>
</html>