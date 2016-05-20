<?php
require_once "PHPMailerAutoload.php";
$email = isset($_POST['email']) ? $_POST['email'] : '';
$user = isset($_POST['user']) ? $_POST['user'] : '';
if(isset($email)){
    $mail = new PHPMailer;
    $mail ->From = "MotionAlert@ryanhancock.co.uk";
    $mail ->FromName = "Motion Alert";
    $mail ->addAddress($email,$user);
    if (isset($_FILES['image']['name'])){
        $mail->addAttachment(basename($_FILES['image']['name']));
    }

    $mail->addReplyTo("MotionAlert@ryanhancock.co.uk", "Reply");
    $mail->isHTML(true);
    $mail->Subject = "Subject Text";
    $mail->Body = "<i>Hello $user you have had a motion alert</i>";
    $mail->AltBody = "Motion Alert";
    if(!$mail->send())
    {
        echo "Mailer Error: " . $mail->ErrorInfo;
    }
    else
    {
        echo "Message has been sent successfully";
    }
}
?>
