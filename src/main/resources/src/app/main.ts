import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app.module';

require('../css/styles.css');
require('../css/menu.css');

platformBrowserDynamic().bootstrapModule(AppModule);
