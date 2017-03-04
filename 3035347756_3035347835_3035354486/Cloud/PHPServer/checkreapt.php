<?php
	$db_server = "sophia.cs.hku.hk";
	$db_user = "zjzeng";
	$db_pwd = "Wgtvf69";
	$link = mysql_connect($db_server, $db_user, $db_pwd) or die(mysql_error());
	mysql_query("set character set 'utf8'");
	mysql_query("set names 'utf8'");

	$db_selected = mysql_select_db($db_user, $link);
	$username = (isset($_GET['username']) ? $_GET['username'] : "");
	$filename = (isset($_GET['filename']) ? $_GET['filename'] : "");

	if (($username != null) and ($filename != null)){
		$sqlcommond = "SELECT filename FROM videos WHERE username=\"".$username."\" AND filename=\"".$filename."\";";
	}

	$sql = "$sqlcommond";
	$res = mysql_query($sql) or die(mysql_error());

	$filename = array();

	$sql = "$sqlcommond";
	$res = mysql_query($sql) or die(mysql_error());
	while ($row = mysql_fetch_array($res)) {
		array_push($filename, $row['filename']);
	}

	if (count($filename) == 0){
		echo "success.";
	}
	else{
		echo "name conflict.";	
	}
?>
