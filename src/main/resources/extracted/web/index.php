<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="icon" href="/favicon.png" type="image/png">
<link rel="stylesheet" href="/assets/styles/global.css" type="text/css" />
<title>Webstats</title>
</head>
<body>
  <header class="<?php echo $online ? 'green' : 'red'; ?>"></header>
  <nav>
    <ul class="box">
      <li>Text</li>
    </ul>
  </nav>
  <article>
    <section class="justified">
      <ul>
        <li><img src="/players/KovuTheHusky.png" alt="KovuTheHusky" class="" /></li>
        <li><img src="/players/NightWolf1298.png" alt="NightWolf1298" class="" /></li>
        <li><img src="/players/kuperfox.png" alt="kuperfox" class="" /></li>
        <li><img src="/players/Cujo27.png" alt="Cujo27" class="" /></li>
        <li><img src="/players/Prince_of_Wars.png" alt="Prince_of_Wars" class="" /></li>
      </ul>
    </section>
    <section class="two justified">
      <div>
        <h1>Server</h1>
        <ul>
          <li><?php echo 'Never';?> Startup</li>
          <li><?php echo 'Never';?> Shutdown</li>
          <li><?php echo '0'; ?>% Uptime</li>
        </ul>
      </div>
      <div>
        <h1>Players</h1>
        <ul>
          <li><?php echo '0'; ?> Online</li>
          <li><?php echo '0'; ?> Tracked</li>
          <li><?php echo '0'; ?> Peak</li>
          <li><?php echo '0'; ?> Maximum</li>
        </ul>
      </div>
    </section>
    <section class="three justified">
      <div>
        <h1>Blocks</h1>
        <ul>
          <li><?php echo '0'; ?> Broken</li>
          <li><?php echo '0'; ?> Placed</li>
          <li><?php echo '0'; ?> Crafted</li>
        </ul>
      </div>
      <div>
        <h1>Items</h1>
        <ul>
          <li><?php echo '0'; ?> Broken</li>
          <li><?php echo '0'; ?> Crafted</li>
          <li><?php echo '0'; ?> Dropped</li>
          <li><?php echo '0'; ?> Picked Up</li>
          <li><?php echo '0'; ?> Used</li>
        </ul>
      </div>
      <div>
        <h1>Deaths</h1>
        <ul>
          <li><?php echo '0'; ?> Kills</li>
          <li><?php echo '0'; ?> Deaths</li>
          <li><?php echo '0'; ?> Most Kills</li>
          <li><?php echo '0'; ?> Most Deaths</li>
        </ul>
      </div>
    </section>
    <section class="three justified">
      <div>
        <h1>Distance</h1>
        <ul>
          <li><?php echo '0'; ?> Total</li>
          <li><?php echo '0'; ?> Walked</li>
          <li><?php echo '0'; ?> Swam</li>
          <li><?php echo '0'; ?> Fallen</li>
          <li><?php echo '0'; ?> Climbed</li>
          <li><?php echo '0'; ?> Flown</li>
          <li><?php echo '0'; ?> Dove</li>
          <li><?php echo '0'; ?> By Minecart</li>
          <li><?php echo '0'; ?> By Boat</li>
          <li><?php echo '0'; ?> By Pig</li>
          <li><?php echo '0'; ?> By Horse</li>
        </ul>
      </div>
      <div>
        <h1>Other</h1>
        <ul>
          <li><?php echo '0'; ?> Eggs Thrown</li>
          <li><?php echo '0'; ?> Eggs Hatched</li>
          <li><?php echo '0'; ?> Chickens Spawned</li>
          <li><?php echo '0'; ?> Fishing Rod Casts</li>
          <li><?php echo '0'; ?> Fish Caught</li>
        </ul>
      </div>
      <div style="visibility: hidden;"></div>
    </section>
  </article>
  <footer>
    Data from <a href="http://dev.bukkit.org/bukkit-mods/webstats">Webstats</a> ${project.version} by <a href="http://codeski.com">Codeski</a>
  </footer>
</body>
</html>