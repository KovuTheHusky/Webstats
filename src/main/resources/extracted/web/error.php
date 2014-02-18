<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

$num = (int)$_GET['error'];
switch ($num) {
	case 100:
		$str = 'That player does not exist.';
		break;
	case 101:
		$str = 'That material does not exist.';
		break;
	case 102:
		$str = 'There is no data for that time period.';
		break;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="icon" href="/favicon.png" type="image/png">
<link rel="stylesheet" href="/assets/styles/global.css" type="text/css" />
<title>Error | Webstats</title>
</head>
<body>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_header.php'; ?>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <article>
    <span style="font-size: 32px;">
      <?php echo $num; ?>: <?php echo $str; ?>
    </span>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>
