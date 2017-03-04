<?php

if ($_FILES["file"]["error"] > 0)
{
	echo "File Error : " . $_FILES["file"]["error"] . "<br />";
	
} else {

	$db_server = "sophia.cs.hku.hk";
	$db_user = "zjzeng";
	$db_pwd = "Wgtvf69";
	$link = mysql_connect($db_server, $db_user, $db_pwd) or die(mysql_error());
	mysql_query("set character set 'utf8'");
	mysql_query("set names 'utf8'");
	$db_selected = mysql_select_db($db_user, $link);

	$pos = strpos($_FILES["file"]["name"],"_");
	$file_type = substr($_FILES["file"]["name"],0,$pos);
	$temp = substr($_FILES["file"]["name"],$pos+1);
	$index = strpos($temp,"_");
	$user_name = substr($temp,0,$index);
	$file_name = substr($temp,$index+1);
	$file_size = $_FILES["file"]["size"] / 1024;

	$sql = "INSERT INTO videos (username,filetype,filesize,filename) VALUES ('$user_name','$file_type',$file_size,'$file_name');";
 	$res = mysql_query($sql) or die(mysql_error());

 	if(!is_dir('upload')){
 		mkdir('upload/');
 		chmod('upload/', 0755);
 	}

	if (!is_dir('upload/'.$user_name.'/')) {
		mkdir('upload/'.$user_name.'/');
		chmod('upload/'.$user_name.'/', 0755);
	}
	
		
	move_uploaded_file($_FILES["file"]["tmp_name"],'upload/'.$user_name.'/'.$file_name);
	chmod('upload/'.$user_name.'/'.$file_name, 0755);
	echo 'success';
	

}

?>