<?php

function playerHead($name) {
	global $uploads_writable;
	if (!$uploads_writable)
		return;
	$source = imagecreatefrompng('http://s3.amazonaws.com/MinecraftSkins/' . $name . '.png');
	if (!$source) {
		copy($_SERVER['DOCUMENT_ROOT'] . '/assets/images/head.png', $_SERVER['DOCUMENT_ROOT'] . '/uploads/' . $name . '.png');
	} else {
		// New width and height
		$width = $height = 128;
		// Resize the first head area
		$head = imagecreatetruecolor($width, $height);
		imagesavealpha($head, true);
		$transparent = imagecolorallocatealpha($head, 0, 0, 0, 127);
		imagefill($head, 0, 0, $transparent);
		imagecopyresized($head, $source, 0, 0, 8, 8, $width, $height, 8, 8);
		// Resize the second head area
		$head2 = imagecreatetruecolor($width, $height);
		imagesavealpha($head2, true);
		$transparent = imagecolorallocatealpha($head2, 0, 0, 0, 127);
		imagefill($head2, 0, 0, $transparent);
		imagecopyresized($head2, $source, 0, 0, 40, 8, $width, $height, 8, 8);
		// Merge the images and save to disk
		imagecopy($head, $head2, 0, 0, 0, 0, $width, $height);
		imagepng($head, $_SERVER['DOCUMENT_ROOT'] . '/uploads/' . $name . '.png');
	}
}

function ws_error($num) {
	ws_redirect("/error/{$num}");
}

function ws_redirect($str) {
	exit(header('Location: http' . (!empty($_SERVER['HTTPS']) ? 's' : '') . '://' . $_SERVER['SERVER_NAME'] . ':' . $_SERVER['SERVER_PORT'] . $str));
}

function ws_enum_encode($str) {
	return strtoupper(str_replace(' ', '_', $str));
}

function ws_enum_decode($str) {
	switch ($str) {
		case 'BOTTLE_O_ENCHANTING':
			return 'Bottle o\\\' Enchanting';
		case 'JACK_O_LANTERN':
			return 'Jack o\\\'Lantern';
		case 'TNT':
			return 'TNT';
	}
	return str_replace(' And ', ' and ', str_replace(' Of ', ' of ', ucwords(strtolower(str_replace('_', ' ', $str)))));
}

function ws_format_distance($num) {
	return $num < 100 ? number_format($num, 0) . 'm' : number_format($num / 1000, 2) . 'km';
}

?>