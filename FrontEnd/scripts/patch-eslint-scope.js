const fs = require('fs');
const path = require('path');

const pkgPaths = [
  path.resolve(__dirname, '../node_modules/eslint-scope/package.json'),
  path.resolve(__dirname, '../node_modules/webpack/node_modules/eslint-scope/package.json')
];

pkgPaths.forEach(pkgPath => {
  if (!fs.existsSync(pkgPath)) {
    console.log(`eslint-scope package not found at ${pkgPath}, skipping patch.`);
    return;
  }

  const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
  if (!pkg.exports) {
    pkg.exports = {};
  }
  if (pkg.main && !pkg.exports['.']) {
    pkg.exports['.'] = pkg.main.startsWith('./') ? pkg.main : './' + pkg.main;
  }
  pkg.exports['./lib/*'] = './lib/*.js';
  pkg.exports['./*'] = './*';
  fs.writeFileSync(pkgPath, JSON.stringify(pkg, null, 2) + '\n', 'utf8');
  console.log(`Patched eslint-scope exports in ${pkgPath}.`);
});
