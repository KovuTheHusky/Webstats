<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';
$materials = array_map('array_shift', $mysqli->query("SELECT type_name FROM ws_material_types ORDER BY type_name ASC")->fetch_all());
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
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_header.php'; ?>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <article>
<?php foreach ($materials as $m) { ?>
    <div class="material">
      <a href="/material/<?php echo strtolower($m); ?>"><img src="/assets/images/materials/<?php echo strtolower($m); ?>.png" alt="<?php echo ws_enum_decode($m); ?>" /></a>
    </div>
<?php } ?>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>
