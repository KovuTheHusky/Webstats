<?php

function playerHead($name) {
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

function morkm($meters) {
	if ($meters >= 100)
		return number_format($meters / 1000, 2) . 'km';
	else
		return number_format($meters, 0) . 'm';
}

?>