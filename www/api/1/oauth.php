<?php
/**
  * OAuth
  */

require_once('/var/www/html/path.php');
require_once('configs/common.php');

require_once('oauth/OAuth.php');

$config = new ApplicationConfig();

$consumer        = new OAuthConsumer($config->key, $config->secret);
$signatureMethod = new OAuthSignatureMethod_HMAC_SHA1();

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

$params = OAuthUtil::parse_parameters($_SERVER['QUERY_STRING']);

// OAuthに必要なURLを取得
$urls->access    = @rawurldecode(@$params['access']);
$urls->request   = @rawurldecode(@$params['request']);
$urls->authorize = @rawurldecode(@$params['authorize']);
$urls->callback  = @rawurldecode(@$params['callback']);

// 削除する
unset($params['access']); 
unset($params['request']);
unset($params['authorize']);
unset($params['callback']);

if ( empty($cache) ) {
    if ( empty($urls->access) || empty($urls->request) || empty($urls->authorize) ) {
	header("Status: 400 Bad Request");
    }
    
    $endpoint = $urls->access;
    $req = OAuthRequest::from_consumer_and_token($consumer, NULL, "GET", $endpoint, $params);
    $req->sign_request($signatureMethod, $consumer, NULL);

    $url      = $req->to_url();
    $header   = array();
    $header   = explode("\n", $req->to_header());

    $curl = curl_init();
    curl_setopt($curl, CURLOPT_CONNECTTIMEOUT,10);
    curl_setopt($curl, CURLOPT_TIMEOUT, 10);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($curl, CURLOPT_HTTPHEADER, $header);
    curl_setopt($curl, CURLOPT_HEADER, false);
    curl_setopt($curl, CURLOPT_URL, $url);

    $responce = curl_exec($curl);
    $error    = curl_error($curl);
    $info     = curl_getinfo($curl);

    curl_close ($curl);

    // エラーが発生した場合
    if ( !empty($error) ) {
	error_log( var_dump("$error", true) );
	header("Status: 500 Internal Server Error");
    }

    if ( empty($responce) || preg_match("/^[[:space:]]+$/", $responce) > 0 ) {
	// Request token
	$endpoint = $urls->request;
	$params = array();

	$req = OAuthRequest::from_consumer_and_token($consumer, NULL, "GET", $endpoint, $params);
	$req->sign_request($signatureMethod, $consumer, NULL);

	$url = $req->to_url();
	$header = array();

	$curl = curl_init();

	curl_setopt($curl, CURLOPT_CONNECTTIMEOUT,10);
	curl_setopt($curl, CURLOPT_TIMEOUT, 10);
	curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);
	curl_setopt($curl, CURLOPT_HTTPHEADER, $header);
	curl_setopt($curl, CURLOPT_HEADER, false);
	curl_setopt($curl, CURLOPT_URL, $url);

	$responce = curl_exec($curl);
	$error    = curl_error($curl);
	$info     = curl_getinfo($curl);

	curl_close ($curl);

	// エラーが発生した場合
	if ( !empty($error) ) {
	    error_log( var_dump("$error", true) );
	    header("Status: 500 Internal Server Error");
	}

	$parsed = OAuthUtil::parse_parameters($responce);
	$token = new OAuthToken($parsed['oauth_token'], $parsed['oauth_token_secret']);

	// Authorize 
	$endpoint = $urls->authorize."?oauth_token={$token->key}";
	http_redirect($endpoint);
    }
    else {
	$parsed = OAuthUtil::parse_parameters($responce);
	$token  = new OAuthToken($parsed['oauth_token'], $parsed['oauth_token_secret']);
	$uid    = $parsed['user_id'];

	$signature = @sha1($uid.$config->secret);
	memcache_set($connect, "$uid:$signature", json_encode($token), 0, 86400); 
	setcookie("twitter_anywhere_identity", "$uid:$signature", null, '/');
    }
}

if ( empty($urls->callback) ) {
    header ("Status: 400 Bad Request");
}

header("Content-Type:text/html; charset=utf-8");
$html = <<<HTML
<html>
	<body>
		<p>Redirecting <a href='{$urls->callback}'>{$urls->callback}</a></p>
	</body>
	<script>window.location.href='{$urls->callback}'</script>
</html>
HTML;
echo $html;

?>
