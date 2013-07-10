<?php

function playerHead($name) {
	$url = 'http://s3.amazonaws.com/MinecraftSkins/' . $name . '.png';
	$percent = 16;
	
	// Get new sizes
	$newwidth = 8 * $percent;
	$newheight = 8 * $percent;
	
	// Load
	$thumb = imagecreatetruecolor($newwidth, $newheight);
	$source = imagecreatefrompng($url);
	
	if (!$source) {
		copy($_SERVER['DOCUMENT_ROOT'] . '/assets/images/head.png', $_SERVER['DOCUMENT_ROOT'] . '/players/' . $name . '.png');
		return;
	}
	
	// Resize
	imagecopyresized($thumb, $source, 0, 0, 8, 8, $newwidth, $newheight, 8, 8);
	
	// Output
	imagepng($thumb, $_SERVER['DOCUMENT_ROOT'] . '/players/' . $name . '.png');
		
}

function morkm($meters) {
	if ($meters < 100)
		return $meters . 'm';
	else
		return number_format($meters / 1000, 2) . 'km';
}

?>