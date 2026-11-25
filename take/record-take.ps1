# Records one IntelliJ coding take for the Spring Boot 4.2 series.
# Run it yourself:  powershell -ExecutionPolicy Bypass -File take\record-take.ps1
# Before: close IntelliJ. During the take (about 8-10 min for 5 files): hands off mouse and keyboard.
# Emergency stop: close this PowerShell window, then run  git checkout -- src  (the typed files may be half done).
# -TestOnly: skip typing, just open the last test file and record the test run (about 2 min).
# -Episode 2: the Spring Boot 4.2 behaviour changes take.
param([switch]$TestOnly, [int]$Episode = 1)
if ($Episode -notin 1, 2, 3, 4) { Write-Host "No take defined for episode $Episode"; exit 1 }

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Windows.Forms
Add-Type @"
using System; using System.Runtime.InteropServices;
public class W { [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr h);
                 [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr h, int c); }
"@

$proj  = Split-Path $PSScriptRoot -Parent
$idea  = 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3\bin\idea64.exe'
$out   = Join-Path $proj "take\take-$(Get-Date -Format yyyyMMdd-HHmmss).mkv"
$pkg   = 'src\main\java\com\deploylab\orders'
$tpkg  = 'src\test\java\com\deploylab\orders'

if (Get-Process idea64 -ErrorAction SilentlyContinue) { Write-Host 'Close IntelliJ first, then run again.'; exit 1 }

# IntelliJ settings (JDK 21, Maven wrapper, no auto-brackets, no pop-ups, terminal JAVA_HOME) are set once by hand.

# Empty files to type into (the tested code lives in take\).
# Episode 1, in the order the script shows them. Exceptions are not typed (they stay in src).
$files = @(
    @{ Dir = $pkg;  Name = 'PaymentClient.java' },
    @{ Dir = $pkg;  Name = 'PaymentConfig.java' },
    @{ Dir = $pkg;  Name = 'OrderController.java' },
    @{ Dir = $tpkg; Name = 'PaymentClientTest.java' },
    @{ Dir = $tpkg; Name = 'OrderControllerTest.java' }
)
$tests = 'PaymentClientTest,OrderControllerTest'
if ($Episode -eq 2) {
    $files = @(
        @{ Dir = 'src\main\resources'; Name = 'application.properties' },
        @{ Dir = $pkg;  Name = 'OrderLinkController.java' },
        @{ Dir = $tpkg; Name = 'ForwardedPrefixTest.java' },
        @{ Dir = $tpkg; Name = 'RelativeRedirectTest.java' },
        @{ Dir = $pkg;  Name = 'InvoiceUploadController.java' },
        @{ Dir = $tpkg; Name = 'InvoiceUploadTest.java' },
        @{ Dir = $tpkg; Name = 'PaidMetricTest.java' }
    )
    $tests = 'ForwardedPrefixTest,RelativeRedirectTest,InvoiceUploadTest,PaidMetricTest'
}
if ($Episode -eq 3) {
    $files = @(
        @{ Dir = $tpkg; Name = 'ConventionsTest.java' },
        @{ Dir = 'src\main\resources'; Name = 'application-otlp.properties' },
        @{ Dir = $tpkg; Name = 'OtlpConfigTest.java' },
        @{ Dir = $pkg;  Name = 'CheckoutController.java' },
        @{ Dir = $tpkg; Name = 'TraceIdKeyTest.java' }
    )
    $tests = 'ConventionsTest,OtlpConfigTest,TraceIdKeyTest'
}
if ($Episode -eq 4) {
    $files = @(
        @{ Dir = $pkg;  Name = 'OrderEventPublisher.java' },
        @{ Dir = $tpkg; Name = 'RabbitStarterTest.java' },
        @{ Dir = $pkg;  Name = 'RefundProperties.java' },
        @{ Dir = $tpkg; Name = 'DefaultValueTest.java' }
    )
    $tests = 'RabbitStarterTest,DefaultValueTest'
}
if (-not $TestOnly) { foreach ($f in $files) { New-Item -Force "$proj\$($f.Dir)\$($f.Name)" -Value '' | Out-Null } }

function Get-IdeaWindow {
    Get-Process idea64 -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like '*orders*' -or $_.MainWindowTitle -like '*springboot*' } | Select-Object -First 1
}
function Focus-Idea { $p = Get-IdeaWindow; [W]::ShowWindow($p.MainWindowHandle, 3) | Out-Null; [W]::SetForegroundWindow($p.MainWindowHandle) | Out-Null; Start-Sleep -Milliseconds 400 }
function Open-InIdea($file) { Start-Process $idea -ArgumentList "`"$file`""; Start-Sleep 3; Focus-Idea }

function Type-Code($path) {
    $rnd = New-Object Random
    foreach ($line in Get-Content $path) {
        $t = $line.TrimStart()            # IntelliJ indents by itself on Enter
        foreach ($ch in $t.ToCharArray()) {
            $k = [string]$ch
            if ('+^%~(){}[]'.Contains($k)) { $k = "{$k}" }
            [System.Windows.Forms.SendKeys]::SendWait($k)
            Start-Sleep -Milliseconds ($rnd.Next(25, 70))
        }
        [System.Windows.Forms.SendKeys]::SendWait('{ENTER}')
        Start-Sleep -Milliseconds ($rnd.Next(120, 300))
    }
    Start-Sleep 1
    [System.Windows.Forms.SendKeys]::SendWait('^%l')   # Reformat Code: fixes any indent IntelliJ got wrong while typing
    Start-Sleep 1
}

Write-Host 'Starting IntelliJ. Hands off from now on.'
Start-Process $idea -ArgumentList "`"$proj`""
for ($i = 0; $i -lt 90 -and -not (Get-IdeaWindow); $i++) { Start-Sleep 2 }
if (-not (Get-IdeaWindow)) { Write-Host 'IntelliJ window not found (first-start dialog open?). Finish it, close IntelliJ, run again.'; exit 1 }
Start-Sleep 30   # let indexing finish

# Record only the monitor IntelliJ is on (keeps the other screen private).
$scr = [System.Windows.Forms.Screen]::FromHandle((Get-IdeaWindow).MainWindowHandle).Bounds
$ff = New-Object System.Diagnostics.Process
$ff.StartInfo.FileName = 'ffmpeg'
$ff.StartInfo.Arguments = "-y -loglevel error -f gdigrab -framerate 30 -offset_x $($scr.X) -offset_y $($scr.Y) -video_size $($scr.Width)x$($scr.Height) -i desktop -c:v libx264 -preset veryfast -crf 20 -pix_fmt yuv420p -f matroska `"$out`""
$ff.StartInfo.UseShellExecute = $false
$ff.StartInfo.RedirectStandardInput = $true
$ff.StartInfo.CreateNoWindow = $true
$ff.Start() | Out-Null
Start-Sleep 2

try {
    if ($TestOnly) { $last = $files[-1]; Open-InIdea "$proj\$($last.Dir)\$($last.Name)"; Start-Sleep 2 }
    else {
        foreach ($f in $files) {
            Open-InIdea "$proj\$($f.Dir)\$($f.Name)"
            Type-Code "$PSScriptRoot\$($f.Name)"
            Start-Sleep 2
        }
    }
    [System.Windows.Forms.SendKeys]::SendWait('^s')          # save all
    Start-Sleep 1
    [System.Windows.Forms.SendKeys]::SendWait('%{F12}')      # IntelliJ terminal
    Start-Sleep 4
    $cmd = ".\mvnw test `"-Dtest=$tests`""   # terminal inherits JAVA_HOME
    foreach ($ch in $cmd.ToCharArray()) { $k=[string]$ch; if ('+^%~(){}[]'.Contains($k)) { $k = "{$k}" }; [System.Windows.Forms.SendKeys]::SendWait($k); Start-Sleep -Milliseconds 30 }
    [System.Windows.Forms.SendKeys]::SendWait('{ENTER}')
    Start-Sleep ([Math]::Min(45 + 15 * ($Episode - 1), 75))   # tests run (episode 2 starts 8 Spring contexts)
}
finally {
    $ff.StandardInput.Write('q'); if (-not $ff.WaitForExit(15000)) { $ff.Kill() }   # mkv stays playable even if cut off
    if (-not $TestOnly) { foreach ($f in $files) { Copy-Item "$PSScriptRoot\$($f.Name)" "$proj\$($f.Dir)\$($f.Name)" -Force } }   # a stopped take never leaves src half-typed
    Write-Host "Done. Recording: $out"
}
