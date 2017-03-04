<?php
    $username = $_GET['username'];
    $password = $_GET['password'];
    $password1 = $_GET['password1'];
    $password2 = $_GET['password2'];
    

    
    if($username == '' || $password == '' || $password1 == '' || $password2 == ''){
        echo 'please fill all values';
    }else if($password1!=$password2){
        echo 'Different input';
    }else{
        require_once('dbConnect.php');
        $sql = "select * from users_info where username='$username' and password='$password'";
        $check = mysqli_fetch_array(mysqli_query($con,$sql));
        if(isset($check)){
            $sql = "update  users_info set password='{$password1}' where username='{$username}'";
            if(mysqli_query($con,$sql)){
                echo 'successfully changed';
            }else{
                echo 'oops! Please try again!';
            }
        }else{
            echo "Invalid Username or Password";
        }
    }
?>
