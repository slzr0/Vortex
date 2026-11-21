const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

const source = fs.readFileSync('app/src/main/java/com/my/vortex/feature_profile/ui/component/DiscordLoginWebView.kt', 'utf8');
const match = source.match(/internal const val TOKEN_SCRIPT = """([\s\S]*?)"""/);
assert.ok(match, 'WebView session script is present');

function run({host = 'discord.com', protocol = 'https:', frameValue = null, directValue = null, frameBlocked = false} = {}) {
    let removed = false;
    const frame = {
        style: {},
        contentWindow: {localStorage: {getItem: () => frameValue}},
        remove() { removed = true; },
    };
    const context = {
        location: {hostname: host, protocol},
        window: {localStorage: {getItem: () => directValue}},
        document: {
            createElement() { return frame; },
            body: {appendChild() { if (frameBlocked) throw new Error('blocked'); }},
        },
    };
    const value = vm.runInNewContext(match[1], context);
    return {value, removed};
}

const token = 'abcDEF0123456789'.repeat(4);
assert.deepEqual(run({frameValue: JSON.stringify(token)}), {value: token, removed: true});
assert.equal(run({frameValue: token}).value, token);
assert.equal(run({frameValue: 'null', directValue: JSON.stringify(token)}).value, token);
assert.equal(run({frameBlocked: true, directValue: JSON.stringify(token)}).value, token);
assert.equal(run({frameValue: null, directValue: null}).value, null);
assert.equal(run({host: 'discord.com.evil.test', frameValue: JSON.stringify(token)}).value, null);
assert.equal(run({protocol: 'http:', frameValue: JSON.stringify(token)}).value, null);
console.log('Discord session extraction checks passed.');
