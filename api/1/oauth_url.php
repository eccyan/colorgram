<?php
/**
 * URLパラメータを JSON 形式で出力する
 */
require_once("/var/www/html/path.php");
require_once("configs/common.php");

require_once('oauth/OAuth.php');

$config = new ApplicationConfig();

// get @Anywhere login value
$anywhereId = explode(':', @$_COOKIE["twitter_anywhere_identity"]);
$anywhere->id        = @$anywhereId[0];
$anywhere->signature = @$anywhereId[1];
// for validate anywhere signature
$signature = @sha1(@$anywhere->id.$config->secret);

$connect = memcache_connect('127.0.0.1', 11211);
$cache = null;
if ( !empty($anywhere->id)  && strcmp($anywhere->signature, $signature) == 0 ) {
    $cache = memcache_get($connect, "{$anywhere->id}:{$anywhere->signature}");
}

$url = null;
if ( !empty($cache) ) {
    $decoded = json_decode($cache);

    $method   = null;
    $endpoint = null;
    $p        = null;
    $parameters = OAuthUtil::parse_parameters($_SERVER['QUERY_STRING']);
    foreach ($parameters as $key => $parameter) {
	if ( strcmp($key, 'm') == 0 ) {
	    $method = $parameter;
	}
	elseif ( strcmp($key, 'ep') == 0 ) {
	    $endpoint = rawurldecode($parameter);
	}
	else {
	    $p[$key] = $parameter;
	}
    }

    $consumer        = new OAuthConsumer($config->key, $config->secret);
    $signatureMethod = new OAuthSignatureMethod_HMAC_SHA1();
    $token           = new OAuthToken($decoded->key, $decoded->secret);

    $req = OAuthRequest::from_consumer_and_token($consumer, $token, $method, $endpoint, $p);
    $req->sign_request($signatureMethod, $consumer, $token);
    $url = $req->to_url();
}

$responce = json_encode($url);

header('Content-type: text/javascript; charset=utf-8');
echo ($responce);
?>
