<?php
  $db_server = "sophia.cs.hku.hk";
	$db_user = "zjzeng";
	$db_pwd = "Wgtvf69";
	$link = mysql_connect($db_server, $db_user, $db_pwd) or die(mysql_error());
  mysql_query("set character set 'utf8'");
  mysql_query("set names 'utf8'");
	$db_selected = mysql_select_db($db_user, $link);

  $username = (isset($_GET['username']) ? $_GET['username'] : "");
  $filetype = (isset($_GET['filetype']) ? $_GET['filetype'] : "");
  $orderby = (isset($_GET['orderby']) ? $_GET['orderby'] : "");

  if($orderby=="default"){
    $orderby = "createtime";
  }
  if($filetype=="all"){
    $sqlcommond = "SELECT filename FROM videos WHERE username=\"".$username."\" ORDER BY ".$orderby." DESC;";
  }
  else{
    $sqlcommond = "SELECT filename FROM videos WHERE username=\"".$username."\" AND filetype=\"".$filetype."\" ORDER BY ".$orderby." DESC;";
  }
  
  $filename = array();

  $sql = "$sqlcommond";
  $res = mysql_query($sql) or die(mysql_error());
  while ($row = mysql_fetch_array($res)) {
    array_push($filename, $row['filename']);
  }
  echo '[';
  $add_delimiter = false;
  for ($i=0; $i<count($filename); $i++) {
    echo ($add_delimiter ? ', ' : '') . '"' . $filename[$i] . '"';
    $add_delimiter = true;
  }
  echo ']';

?>
