<?php 
  $username = (isset($_GET['username']) ? $_GET['username'] : "");
  $filename = (isset($_GET['filename']) ? $_GET['filename'] : "");
  $filepath = "./upload/".$username."/".$filename;
  chmod($filepath.'/', 0755);
  unlink($filepath);
	
  $db_server = "sophia.cs.hku.hk";
  $db_user = "zjzeng";
  $db_pwd = "Wgtvf69";
  $link = mysql_connect($db_server, $db_user, $db_pwd) or die(mysql_error());
  $db_selected = mysql_select_db($db_user, $link);
	
  $sqlcommond = "DELETE FROM videos WHERE username=\"".$username."\" AND filename=\"".$filename."\";";

  $sql = "$sqlcommond";
  $res = mysql_query($sql) or die(mysql_error());

  echo "delete successfuly."
?>