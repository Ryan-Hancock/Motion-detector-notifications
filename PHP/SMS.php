<?php
include "smsGateway.php";
$num= isset($_GET['num']) ? $_GET['num'] : '';
$smsGateway = new SmsGateway('derazzaboy@gmail.com', 'marvel16');

$deviceID = 20319;
$number = $num;
$message = 'Motion Detected on device';


//Please note options is no required and can be left out
$result = $smsGateway->sendMessageToNumber($number, $message, $deviceID);
echo json_encode($result);
?>