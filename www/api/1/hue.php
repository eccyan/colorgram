<?php
/**
 * 指定URL画像の色相を取得する
 */
require_once("/var/www/html/path.php");
require_once("configs/common.php");

$config = new ApplicationConfig();

$socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
if ($socket === false) {
}

socket_connect($socket, $config->hueServer, $config->huePort);

$responce = json_encode($url);

header('Content-type: text/javascript; charset=utf-8');
echo ($responce);
?>
