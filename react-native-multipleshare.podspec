require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']
  s.license      = package['license']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platform     = :ios, "9.0"

  s.source       = { :git => "https://github.com/yiky84119/react-native-multipleshare.git", :tag => "v#{s.version}" }
  s.source_files  = "ios/**/*.{h,m}"

  s.frameworks = "UIKit", "SystemConfiguration", "CoreTelephony", "QuartzCore", "CoreText", "CoreGraphics", "Foundation", "CFNetWork", "CoreMotion"
  s.libraries = "c++", "z"

  s.dependency 'React'
end
