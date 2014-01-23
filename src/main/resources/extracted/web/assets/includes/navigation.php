<?php $page = substr($_SERVER['PHP_SELF'], 1, strrpos($_SERVER['PHP_SELF'], '.') - 1); ?>
  <nav class="large">
    <ul>
      <li<?php if ($page == 'index') echo ' class="selected"'; ?>><a href="/">Home</a></li>
      <li<?php if ($page == 'damages' || $page == 'damage') echo ' class="selected"'; ?>><a href="/damages">Damages</a></li>
      <li<?php if ($page == 'distances' || $page == 'distance') echo ' class="selected"'; ?>><a href="/distances">Distances</a></li>
      <li<?php if ($page == 'materials' || $page == 'material') echo ' class="selected"'; ?>><a href="/materials">Materials</a></li>
      <li<?php if ($page == 'players' || $page == 'player') echo ' class="selected"'; ?>><a href="/players">Players</a></li>
    </ul>
  </nav>
