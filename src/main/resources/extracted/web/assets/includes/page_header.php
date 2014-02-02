<?php if (isset($error)) { foreach ($error as $e) { ?>
  <div style="margin: 10px auto;"><span style="color: red; font-weight: bold;">Error:</span> <?php echo $e; ?></div>
<?php } } ?>
  <header class="<?php echo $online ? 'green' : 'red'; ?>"></header>
