# Wahoo Android app

A thin wrapper around your existing web game. It opens the **same** `/play` page you use in a browser
(so school/web play keeps working and updates the moment you re-deploy), and adds a native Cast icon
that sends your **TV page** to a Chromecast. No mirroring.

## Get the APK without installing anything (GitHub)
1. Make a free GitHub repo and upload everything in this folder (keep the `.github` folder).
2. Open the repo's **Actions** tab -> **Build APK** -> **Run workflow** (it also runs on every push).
3. When it's green, open the run and download the **wahoo-apk** artifact. Unzip it, copy
   `app-debug.apk` to the phone, and install it (allow "install unknown apps" when asked).

Or open this folder in Android Studio and press Run.

## Before you build, check two addresses
- `app/src/main/res/values/strings.xml` -> `start_url` (your play page).
- In `play.html`, `CFG.tvUrl` (your TV page; currently `https://wahoogame302.web.app/tv`).

Both pages must be on **https**, and you must re-deploy the new `tv.html` / `play.html` to your hosting.

## How casting works
Chromecasts have no web browser. They only run "receiver apps", and registering your own costs US$5.
This app uses **DashCast** (a free, community-made receiver, app id `84912283`) which opens any web
page. The Cast sender SDK itself is free. The app sends DashCast the link `<tvUrl>?room=1234`,
the TV page opens with that room code, and the phones join it.

## Honest caveats
- DashCast is run by volunteers, not Google. It can break or vanish. If it ever does, the only other
  ways are mirroring a tab, or registering your own receiver (US$5, one time).
- DashCast shows the page in a frame at about 1280x720. The TV page scales to fit.
- I couldn't compile or run the app where I built it, so treat the first build as untested. If the
  GitHub build fails, send me the red error text from the Actions log.
