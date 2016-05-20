<?php
require_once 'DBconfig.php';
$name= isset($_POST['name']) ? $_POST['name'] : '';
$motion= isset($_POST['motion']) ? $_POST['motion'] : '';
$set= isset($_POST['set']) ? $_POST['set'] : '';
$image = isset($_POST['image']) ? $_POST['image'] : '';
$response = array();
$conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_DATABASE);
$target_path = "uploads/$name/";
$server_ip = gethostbyname(gethostname());
$file_upload_url = 'http://' .'ryanhancock.co.uk' . '/' . 'AndroidFileUpload' . '/' . $target_path;
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
    echo "error";
}
$sql = "SELECT remote FROM users WHERE unique_id = '$name'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    // output data of each row
    while($row = $result->fetch_assoc()) {
        $response['status']= $row["remote"];
        $response['error']='ok';
    }
} else {
    $response['error']= "fail";
    $response['user']= $name;
}
if ($set == "0"){
    $upsql = "UPDATE users SET remote = 0 WHERE unique_id='$name'";
    if ($conn->query($upsql) === TRUE) {
        $response['setError']="success";
    } else {
        $response['setError']= $conn->error;
    }
}
if (isset($motion)){
    $mosql = "UPDATE users SET motion = '$motion' WHERE unique_id='$name'";
    if ($conn->query($mosql) === TRUE) {
        $response['moError']="success";
    } else {
        $response['moError']= $conn->error;
    }
}
if (isset($image)){
        $response['imageurl'] = $file_upload_url . $image;
}

$sql =
$response['motion']=$motion;
$status = json_encode($response);
$statsql = "UPDATE users SET status = '$status' WHERE unique_id='$name'";
if ($conn->query($statsql) === TRUE) {
    $response['statError']="success";
} else {
    $response['statError']= $conn->error;
}

$conn->close();
echo json_encode($response);
?>