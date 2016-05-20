<?php
$name = isset($_GET['name']) ? $_GET['name'] : '';
//$name = isset($_POST['name']) ? $_POST['name'] : '';
$file;
//$files = array();
$log_directory = ('uploads/'.$name);
foreach(glob($log_directory.'/*.*') as $file) {
    $files[] = 
	 array ('title'=> $file, 'url' =>"http://ryanhancock.co.uk/AndroidFileUpload/".$file,
					'width' => "640",
					'height' => "320"
	);
}
header('Content-type: application/json');
$arr = array ('images' => array ('image' => $files) );
echo json_encode($arr);
?>
