(function (window) {
    'use strict';

    var appShell = window.appShell;
    if (!appShell) {
        return;
    }

    var apiRoot = (appShell.contextPath || '') + '/api';

    function buildOptions(method, payload, options) {
        var opts = options ? Object.assign({}, options) : {};
        opts.method = method;
        opts.headers = new Headers(opts.headers || {});
        var token = window.localStorage.getItem('auth_token');
        if (token && !opts.headers.has('Authorization')) {
            opts.headers.set('Authorization', 'Bearer ' + token);
        }
        var hasBody = payload !== undefined && payload !== null;
        if (hasBody && !(payload instanceof FormData) && !opts.headers.has('Content-Type')) {
            opts.headers.set('Content-Type', 'application/json; charset=UTF-8');
            opts.body = JSON.stringify(payload);
        } else if (hasBody && payload instanceof FormData) {
            opts.body = payload;
        } else if (opts.body && typeof opts.body === 'object' && !(opts.body instanceof FormData)) {
            if (!opts.headers.has('Content-Type')) {
                opts.headers.set('Content-Type', 'application/json; charset=UTF-8');
            }
            opts.body = JSON.stringify(opts.body);
        }
        opts.credentials = opts.credentials || 'include';
        return opts;
    }

    async function request(path, options) {
        var target = apiRoot + path;
        var response = await fetch(target, buildOptions(options && options.method ? options.method : 'GET', options && options.body, options));
        var data = null;
        var text = await response.text();
        if (text && text.trim().length > 0) {
            try {
                data = JSON.parse(text);
            } catch (err) {
                console.warn('apiClient: response is not JSON', err);
            }
        }
        if (!response.ok) {
            var error = new Error('API request failed with status ' + response.status);
            error.status = response.status;
            error.payload = data;
            throw error;
        }
        return data;
    }

    function get(path, options) {
        return request(path, Object.assign({}, options, { method: 'GET' }));
    }

    function post(path, body, options) {
        var opts = Object.assign({}, options, { method: 'POST', body: body });
        return request(path, opts);
    }

    function put(path, body, options) {
        var opts = Object.assign({}, options, { method: 'PUT', body: body });
        return request(path, opts);
    }

    function del(path, options) {
        return request(path, Object.assign({}, options, { method: 'DELETE' }));
    }

    window.apiClient = {
        root: apiRoot,
        request: request,
        get: get,
        post: post,
        put: put,
        del: del
    };
})(window);
